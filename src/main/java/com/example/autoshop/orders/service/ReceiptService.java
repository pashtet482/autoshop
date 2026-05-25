package com.example.autoshop.orders.service;

import com.example.autoshop.orders.model.Order;
import com.example.autoshop.orders.model.ProductInOrder;
import com.example.autoshop.orders.repository.OrderRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReceiptService {
    @Value("${app.tax.percent:22}")
    private BigDecimal taxPercent;

    private final OrderRepository orderRepository;

    public byte[] generateReceipt(Long orderId,
                                  String currentUsername,
                                  boolean adminMode) {

        Order order = findAccessibleOrder(orderId, currentUsername, adminMode);

        String html = buildHtml(order);

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.withHtmlContent(html, null);

            builder.useFont(
                    () -> getClass()
                            .getResourceAsStream(
                                    "/fonts/DejaVuSans.ttf"
                            ),
                    "DejaVu Sans"
            );

            builder.toStream(outputStream);

            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate PDF",
                    e
            );
        }
    }

    private @NonNull String buildHtml(@NonNull Order order) {

        StringBuilder itemsHtml = new StringBuilder();

        for (ProductInOrder item : order.getItems()) {

            itemsHtml.append("""
                <tr>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%s ₽</td>
                </tr>
                """.formatted(
                    item.getProduct().getName(),
                    item.getQuantity(),
                    formatDecimal(item.getPriceAtPurchase())
            ));
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy");

        BigDecimal originalTotal = order.getItems()
                .stream()
                .map(item ->
                        item.getPriceAtPurchase().multiply(
                                java.math.BigDecimal.valueOf(
                                        item.getQuantity()
                                )
                        )
                )
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        BigDecimal ratio =
                order.getUser()
                        .getPriceLevel()
                        .getRatio();

        BigDecimal discountedSubtotal = originalTotal.multiply(ratio);
        BigDecimal taxAmount = discountedSubtotal.multiply(taxPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = order.getTotalPrice();
        BigDecimal discountAmount = originalTotal.subtract(discountedSubtotal);

        BigDecimal discountPercent =
                java.math.BigDecimal.ONE
                        .subtract(ratio)
                        .multiply(java.math.BigDecimal.valueOf(100));

        return """
            <!DOCTYPE html>

            <html xmlns="http://www.w3.org/1999/xhtml">

            <head>

                <meta charset="UTF-8" />

                <style>

                    body {
                        font-family: DejaVu Sans;
                        padding: 20px;
                    }

                    h1 {
                        color: #c62828;
                    }

                    table {
                        width: 100%%;
                        border-collapse: collapse;
                        margin-top: 20px;
                    }

                    th, td {
                        border: 1px solid #cccccc;
                        padding: 10px;
                        text-align: left;
                    }

                    th {
                        background-color: #f5f5f5;
                    }

                    .total {
                        margin-top: 20px;
                        font-size: 18px;
                        font-weight: bold;
                    }

                </style>

            </head>

            <body>

                <h1>Чек AutoShop</h1>

                <p>
                    <strong>Номер заказа:</strong>
                    %d
                </p>

                <p>
                    <strong>Покупатель:</strong>
                    %s
                </p>

                <p>
                    <strong>Дата заказа:</strong>
                    %s
                </p>

                <p>
                    <strong>Дата доставки:</strong>
                    %s
                </p>

                <p>
                    <strong>Адрес доставки:</strong>
                    %s
                </p>

                <table>

                    <thead>
                        <tr>
                            <th>Товар</th>
                            <th>Количество</th>
                            <th>Цена</th>
                        </tr>
                    </thead>

                    <tbody>
                        %s
                    </tbody>

                </table>

                <div class="total">
            
                        <p>
                            Сумма без скидки:
                            %s ₽
                        </p>
            
                        <p>
                            Тип скидки:
                            Скидка по уровню цены
                        </p>

                        <p>
                            Скидка:
                            %s%% (%s ₽)
                        </p>

                        <p>
                            Налог:
                            %s%% (%s ₽)
                        </p>
            
                        <p>
                            Итоговая сумма:
                            %s ₽
                        </p>
            
                    </div>

            </body>

            </html>
            """.formatted(
                order.getId(),
                order.getUser().getUsername(),
                order.getDateOfPurchase().format(formatter),
                order.getDateOfDelivery().format(formatter),
                order.getDeliveryAddress(),
                itemsHtml.toString(),
                formatDecimal(originalTotal),
                formatDecimal(discountPercent),
                formatDecimal(discountAmount),
                formatDecimal(taxPercent),
                formatDecimal(taxAmount),
                formatDecimal(finalTotal)
        );
    }

    private @NonNull String formatDecimal(@NonNull BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private @NonNull Order findAccessibleOrder(Long orderId,
                                               String currentUsername,
                                               boolean adminMode) {
        Order order = orderRepository.findById(orderId)
                .filter(Order::isActive)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")
                );

        if (!adminMode && !order.getUser().getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден");
        }

        return order;
    }
}

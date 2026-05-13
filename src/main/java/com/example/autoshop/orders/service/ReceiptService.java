package com.example.autoshop.orders.service;

import com.example.autoshop.orders.model.Order;
import com.example.autoshop.orders.model.ProductInOrder;
import com.example.autoshop.orders.repository.OrderRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final OrderRepository orderRepository;

    public byte[] generateReceipt(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found")
                );

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
                    item.getPriceAtPurchase()
            ));
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
                    Итоговая сумма: %s ₽
                </div>

            </body>

            </html>
            """.formatted(
                order.getId(),
                order.getUser().getUsername(),
                order.getDateOfPurchase().format(formatter),
                order.getDateOfDelivery().format(formatter),
                itemsHtml.toString(),
                order.getTotalPrice()
        );
    }
}
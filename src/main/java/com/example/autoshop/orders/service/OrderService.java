package com.example.autoshop.orders.service;

import com.example.autoshop.orders.dto.InputOrderDTO;
import com.example.autoshop.orders.dto.InputOrderItemDTO;
import com.example.autoshop.orders.dto.OrderDTO;
import com.example.autoshop.orders.mapper.OrderMapper;
import com.example.autoshop.orders.mapper.ProductInOrderMapper;
import com.example.autoshop.orders.model.Order;
import com.example.autoshop.orders.model.OrderStatus;
import com.example.autoshop.orders.model.ProductInOrder;
import com.example.autoshop.orders.repository.OrderRepository;
import com.example.autoshop.products.model.Product;
import com.example.autoshop.products.model.ProductStock;
import com.example.autoshop.products.repository.ProductInOrderRepository;
import com.example.autoshop.products.repository.ProductRepository;
import com.example.autoshop.products.repository.ProductStockRepository;
import com.example.autoshop.users.model.User;
import com.example.autoshop.users.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderMapper orderMapper;

    private final ProductInOrderMapper productInOrderMapper;
    private final OrderRepository orderRepository;
    private final ProductStockRepository productStockRepository;
    private final ProductInOrderRepository productInOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderDTO createOrder(@NonNull InputOrderDTO dto,
                                String currentUsername,
                                boolean adminMode) {

        User user = adminMode && dto.userId() != null
                ? findUserById(dto.userId())
                : findUserByUsername(currentUsername);

        Order order = orderMapper.toEntity(dto);

        order.setUser(user);
        order.setOrderStatus(OrderStatus.CREATED);
        order.setDateOfPurchase(OffsetDateTime.now());
        order.setDateOfDelivery(
                OffsetDateTime.now().plusDays(1)
        );
        order.setTotalPrice(BigDecimal.ZERO);
        order.setUpdatedAt(OffsetDateTime.now());

        BigDecimal totalPrice = BigDecimal.ZERO;

        Order savedOrder = orderRepository.save(order);

        List<ProductInOrder> items = new ArrayList<>(
                dto.items()
                        .stream()
                        .map(productInOrderMapper::toEntity)
                        .toList()
        );

        int maxDeliveryDays = 0;
        for (int i = 0; i < items.size(); i++) {

            ProductInOrder item = items.get(i);
            InputOrderItemDTO itemDto = dto.items().get(i);

            Product product = findProductById(itemDto.productId());

            int totalStock = product.getStocks()
                    .stream()
                    .map(ProductStock::getQuantity)
                    .reduce(0, Integer::sum);

            if (totalStock < itemDto.quantity()) {
                throw new RuntimeException(
                        "Недостаточно товара на складе"
                );
            }

            ProductStock stock = product.getStocks()
                    .stream()
                    .filter(s -> s.getQuantity() > 0)
                    .findFirst()
                    .orElseThrow();

            maxDeliveryDays = Math.max(
                    maxDeliveryDays,
                    stock.getWarehouse().getDeliveryDays()
            );

            stock.setQuantity(
                    stock.getQuantity() - itemDto.quantity()
            );

            order.setDateOfDelivery(
                    OffsetDateTime.now()
                            .plusDays(maxDeliveryDays)
            );

            productStockRepository.save(stock);

            item.setOrder(savedOrder);
            item.setProduct(product);

            BigDecimal price = product.getSellingPrice();

            item.setPriceAtPurchase(price);

            BigDecimal itemTotal = price.multiply(
                    BigDecimal.valueOf(itemDto.quantity())
            );

            totalPrice = totalPrice.add(itemTotal);
        }

        BigDecimal ratio = user.getPriceLevel().getRatio();

        totalPrice = totalPrice.multiply(ratio);

        savedOrder.setTotalPrice(totalPrice);

        productInOrderRepository.saveAll(items);

        savedOrder.setItems(items);

        orderRepository.save(savedOrder);

        return orderMapper.toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id,
                                 String currentUsername,
                                 boolean adminMode) {

        return orderMapper.toDto(findAccessibleOrder(id, currentUsername, adminMode));
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(
            int page,
            int size,
            String currentUsername,
            boolean adminMode
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        return (adminMode
                ? orderRepository.findAll(pageable)
                : orderRepository.findAllByUser_Username(currentUsername, pageable))
                .map(orderMapper::toDto);
    }

    public OrderDTO updateStatus(
            Long id,
            OrderStatus status
    ) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")
                );

        order.setOrderStatus(status);
        order.setUpdatedAt(OffsetDateTime.now());

        return orderMapper.toDto(
                orderRepository.save(order)
        );
    }

    public void deleteOrder(Long id) {

        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден");
        }

        orderRepository.deleteById(id);
    }

    private @NonNull User findUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")
                );
    }

    private @NonNull User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")
                );
    }

    private @NonNull Product findProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден")
                );
    }

    private @NonNull Order findAccessibleOrder(Long id,
                                               String currentUsername,
                                               boolean adminMode) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")
                );

        if (!adminMode && !order.getUser().getUsername().equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден");
        }

        return order;
    }
}

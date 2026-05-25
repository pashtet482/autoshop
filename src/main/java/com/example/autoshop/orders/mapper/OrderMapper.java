package com.example.autoshop.orders.mapper;

import com.example.autoshop.orders.dto.InputOrderDTO;
import com.example.autoshop.orders.dto.OrderDTO;
import com.example.autoshop.orders.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        uses = ProductInOrderMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "dateOfPurchase", ignore = true)
    @Mapping(target = "dateOfDelivery", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(InputOrderDTO dto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "discountType", ignore = true)
    @Mapping(target = "discountPercent", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "taxPercent", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    OrderDTO toDto(Order order);
}

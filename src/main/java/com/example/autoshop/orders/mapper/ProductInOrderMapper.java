package com.example.autoshop.orders.mapper;

import com.example.autoshop.orders.dto.InputOrderItemDTO;
import com.example.autoshop.orders.dto.OrderItemDTO;
import com.example.autoshop.orders.model.ProductInOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductInOrderMapper {

    @Mapping(target = "priceAtPurchase", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductInOrder toEntity(InputOrderItemDTO dto);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    OrderItemDTO toDto(ProductInOrder entity);
}

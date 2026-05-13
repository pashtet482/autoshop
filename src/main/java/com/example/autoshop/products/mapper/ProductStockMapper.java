package com.example.autoshop.products.mapper;

import com.example.autoshop.products.dto.ProductStockDTO;
import com.example.autoshop.products.model.ProductStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductStockMapper {

    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.address", target = "warehouseAddress")
    @Mapping(source = "warehouse.deliveryDays", target = "warehouseDeliveryDays")
    ProductStockDTO toDto(ProductStock stock);
}

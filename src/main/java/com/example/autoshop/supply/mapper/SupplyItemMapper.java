package com.example.autoshop.supply.mapper;

import com.example.autoshop.supply.dto.InputSupplyItemDTO;
import com.example.autoshop.supply.dto.SupplyItemDTO;
import com.example.autoshop.supply.model.SupplyItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SupplyItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "supply", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    SupplyItem toEntity(InputSupplyItemDTO dto);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    SupplyItemDTO toDto(SupplyItem supplyItem);
}
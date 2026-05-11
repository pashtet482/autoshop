package com.example.autoshop.products.mapper;

import com.example.autoshop.products.dto.InputWarehouseDTO;
import com.example.autoshop.products.dto.WarehouseDTO;
import com.example.autoshop.supply.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseMapper {
    @Mapping(target = "id", ignore = true)
    Warehouse toEntity(InputWarehouseDTO dto);

    WarehouseDTO toDto(Warehouse warehouse);

    @Mapping(target = "id", ignore = true)
    void updateWarehouse(InputWarehouseDTO dto, @MappingTarget Warehouse warehouse);
}

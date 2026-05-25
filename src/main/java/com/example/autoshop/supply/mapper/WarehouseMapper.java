package com.example.autoshop.supply.mapper;

import com.example.autoshop.supply.dto.InputWarehouseDTO;
import com.example.autoshop.supply.dto.WarehouseDTO;
import com.example.autoshop.supply.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Warehouse toEntity(InputWarehouseDTO dto);

    WarehouseDTO toDto(Warehouse warehouse);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateWarehouse(InputWarehouseDTO dto, @MappingTarget Warehouse warehouse);
}

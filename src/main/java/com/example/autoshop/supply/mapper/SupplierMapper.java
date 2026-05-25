package com.example.autoshop.supply.mapper;

import com.example.autoshop.supply.dto.InputSupplierDTO;
import com.example.autoshop.supply.dto.SupplierDTO;
import com.example.autoshop.supply.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Supplier toEntity(InputSupplierDTO dto);

    SupplierDTO toDto(Supplier supplier);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateSupplier(InputSupplierDTO dto, @MappingTarget Supplier supplier);
}

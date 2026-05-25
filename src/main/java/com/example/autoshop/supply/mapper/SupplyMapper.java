package com.example.autoshop.supply.mapper;

import com.example.autoshop.supply.dto.InputSupplyDTO;
import com.example.autoshop.supply.dto.SupplyDTO;
import com.example.autoshop.supply.model.Supply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        uses = {SupplyItemMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SupplyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "supplierId", target = "supplier.id")
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Supply toEntity(InputSupplyDTO dto);

    @Mapping(source = "supplier.id", target = "supplierId")
    SupplyDTO toDto(Supply supply);

    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateSupply(
            InputSupplyDTO dto,
            @MappingTarget Supply supply
    );
}

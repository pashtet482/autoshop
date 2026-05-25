package com.example.autoshop.products.mapper;

import com.example.autoshop.products.dto.BrandDTO;
import com.example.autoshop.products.dto.InputBrandDTO;
import com.example.autoshop.products.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BrandMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Brand toEntity(InputBrandDTO dto);

    BrandDTO toDto(Brand brand);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateBrand(InputBrandDTO dto, @MappingTarget Brand brand);
}

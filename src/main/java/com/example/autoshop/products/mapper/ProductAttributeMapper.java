package com.example.autoshop.products.mapper;

import com.example.autoshop.products.dto.ProductAttributeDTO;
import com.example.autoshop.products.model.ProductAttribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductAttributeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductAttribute toEntity(ProductAttributeDTO dto);

    ProductAttributeDTO toDto(ProductAttribute attribute);
}

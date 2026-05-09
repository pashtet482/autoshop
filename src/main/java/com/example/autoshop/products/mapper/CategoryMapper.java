package com.example.autoshop.products.mapper;

import com.example.autoshop.products.dto.CategoryDTO;
import com.example.autoshop.products.dto.InputCategoryDto;
import com.example.autoshop.products.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toEntity(InputCategoryDto dto);

    CategoryDTO toDto(Category category);

    @Mapping(target = "id", ignore = true)
    void updateProduct(InputCategoryDto dto, @MappingTarget Category category);
}

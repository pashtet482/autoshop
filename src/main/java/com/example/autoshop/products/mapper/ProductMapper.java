package com.example.autoshop.products.mapper;

import com.example.autoshop.products.dto.CreateProductDTO;
import com.example.autoshop.products.dto.ProductDTO;
import com.example.autoshop.products.dto.UpdateProductDTO;
import com.example.autoshop.products.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper{
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    @Mapping(target = "id", ignore = true)
    Product createProduct(CreateProductDTO dto);

    ProductDTO toDto(Product product);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "stocks", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateProduct(UpdateProductDTO dto, @MappingTarget Product product);
}

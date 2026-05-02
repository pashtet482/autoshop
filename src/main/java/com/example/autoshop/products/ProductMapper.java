package com.example.autoshop.products;

import com.example.autoshop.products.dto.ProductDTO;
import com.example.autoshop.products.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper{
    Product toEntity(ProductDTO dto);
    ProductDTO toDto(Product product);
    void updateProduct(ProductDTO dto, @MappingTarget Product product);
}

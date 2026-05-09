package com.example.autoshop.products.service;

import com.example.autoshop.products.mapper.ProductMapper;
import com.example.autoshop.products.dto.*;
import com.example.autoshop.products.model.*;
import com.example.autoshop.products.repository.BrandRepository;
import com.example.autoshop.products.repository.CategoryRepository;
import com.example.autoshop.products.repository.ProductRepository;
import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    public ProductDTO getProductById(Long id){
        return productMapper.toDto(findProductById(id));
    }

    public ProductDTO createProduct(CreateProductDTO dto){
        Product product = productMapper.createProduct(dto);

        product.setBrand(findBrandById(dto.brandId()));
        product.setCategory(findCategoryById(dto.categoryId()));

        if (product.getAttributes() != null) {
            product.getAttributes().forEach(attr -> attr.setProduct(product));
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    public ProductDTO updateProduct(@NonNull UpdateProductDTO dto, Long id){
        Product product = findProductById(id);

        productMapper.updateProduct(dto, product);
        product.setBrand(findBrandById(dto.brandId()));
        product.setCategory(findCategoryById(dto.categoryId()));

        if (product.getAttributes() != null) {
            product.getAttributes().forEach(attr -> attr.setProduct(product));
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    public void deleteProduct(Long id){
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    private @NonNull Product findProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + id + " not found"));
    }

    private @NonNull Brand findBrandById(Long id){
        return brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand with id: " + id + " not found"));
    }

    private @NonNull Category findCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with id: " + id + " not found"));
    }

    public Page<ProductDTO> getFilteredProducts(@NonNull ProductSearchFilterDTO filter,
                                                int page,
                                                int size,
                                                String sortBy,
                                                String sortDirection
    ) {
        QProduct product = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder();

        if (filter.name() != null && !filter.name().isBlank()) {
            builder.and(product.name.containsIgnoreCase(filter.name()));
        }

        if (filter.minPrice() != null) {
            builder.and(product.sellingPrice.goe(filter.minPrice()));
        }

        if (filter.maxPrice() != null) {
            builder.and(product.sellingPrice.loe(filter.maxPrice()));
        }

        if (filter.categoryId() != null) {
            builder.and(product.category.id.eq(filter.categoryId()));
        }

        if (filter.brandId() != null) {
            builder.and(product.brand.id.eq(filter.brandId()));
        }

        if (filter.sku() != null && !filter.sku().isBlank()) {
            builder.and(product.sku.containsIgnoreCase(filter.sku()));
        }

        if (filter.oemNumber() != null && !filter.oemNumber().isBlank()) {
            builder.and(product.oemNumber.containsIgnoreCase(filter.oemNumber()));
        }

        if (Boolean.TRUE.equals(filter.inStock())) {
            builder.and(product.stocks.any().quantity.gt(0));
        }

        if (filter.attributes() != null && !filter.attributes().isEmpty()) {
            for (Map.Entry<String, List<String>> entry : filter.attributes().entrySet()) {

                String attrName = entry.getKey();
                List<String> attrValues = entry.getValue();

                if (attrValues != null && !attrValues.isEmpty()) {
                    builder.and(
                            product.attributes.any().name.eq(attrName)
                                    .and(product.attributes.any().value.in(attrValues))
                    );
                }
            }
        }

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> results = productRepository.findAll(builder, pageable);

        return results.map(productMapper::toDto);
    }
}

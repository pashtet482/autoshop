package com.example.autoshop.products.service;

import com.example.autoshop.products.ProductMapper;
import com.example.autoshop.products.dto.ProductDTO;
import com.example.autoshop.products.dto.ProductSearchFilterDTO;
import com.example.autoshop.products.model.Product;
import com.example.autoshop.products.model.QProduct;
import com.example.autoshop.products.model.QProductAttribute;
import com.example.autoshop.products.model.QProductStock;
import com.example.autoshop.products.repository.ProductRepository;
import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductDTO> getAllProducts(){
        return productRepository.findAllBy();
    }

    public ProductDTO getProductById(Long id){
        return productMapper.toDto(findProductById(id));
    }

    public ProductDTO createProduct(ProductDTO dto){
        Product product = productMapper.toEntity(dto);
        productRepository.save(product);
        return productMapper.toDto(product);
    }

    public ProductDTO updateProduct(@NonNull ProductDTO dto, Long id){
        Product product = findProductById(id);
        productMapper.updateProduct(dto, product);
        productRepository.save(product);
        return productMapper.toDto(product);
    }

    public void deleteProduct(Long id){
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    private @NonNull Product findProductById(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + id + " not found"));
    }

    public List<ProductDTO> getFilteredProducts(@NonNull ProductSearchFilterDTO filter) {
        QProduct product = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder();

        if (filter.name() != null && !filter.name().isBlank()){
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

        if (filter.sku() != null) {
            builder.and(product.sku.containsIgnoreCase(filter.sku()));
        }
        if (filter.oemNumber() != null) {
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
                    builder.and(product.attributes.any().name.eq(attrName)
                            .and(product.attributes.any().value.in(attrValues)));
                }
            }
        }

        Iterable<Product> results = productRepository.findAll(builder);

        return StreamSupport.stream(results.spliterator(), false)
                .distinct()
                .map(productMapper::toDto)
                .toList();
    }
}

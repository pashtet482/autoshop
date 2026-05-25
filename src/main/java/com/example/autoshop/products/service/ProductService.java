package com.example.autoshop.products.service;

import com.example.autoshop.products.mapper.ProductMapper;
import com.example.autoshop.products.dto.*;
import com.example.autoshop.products.model.*;
import com.example.autoshop.products.repository.BrandRepository;
import com.example.autoshop.products.repository.CategoryRepository;
import com.example.autoshop.products.repository.ProductRepository;
import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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
        product.markDeleted();
        productRepository.save(product);
    }

    private @NonNull Product findProductById(Long id){
        return productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + id + " not found"));
    }

    private @NonNull Brand findBrandById(Long id){
        return brandRepository.findById(id)
                .filter(Brand::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Brand with id: " + id + " not found"));
    }

    private @NonNull Category findCategoryById(Long id){
        return categoryRepository.findById(id)
                .filter(Category::isActive)
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
        builder.and(product.isDeleted.isFalse());

        String commonQuery = null;
        if (filter.query() != null && !filter.query().isBlank()) {
            commonQuery = filter.query();
        } else if (filter.name() != null && !filter.name().isBlank()
                && filter.name().equals(filter.sku())
                && filter.name().equals(filter.oemNumber())) {
            commonQuery = filter.name();
        }

        if (commonQuery != null) {
            builder.and(
                    product.name.containsIgnoreCase(commonQuery)
                            .or(product.sku.containsIgnoreCase(commonQuery))
                            .or(product.oemNumber.containsIgnoreCase(commonQuery))
            );
        }

        if (commonQuery == null && filter.name() != null && !filter.name().isBlank()) {
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

        if (commonQuery == null && filter.sku() != null && !filter.sku().isBlank()) {
            builder.and(product.sku.containsIgnoreCase(filter.sku()));
        }

        if (commonQuery == null && filter.oemNumber() != null && !filter.oemNumber().isBlank()) {
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

    public ProductDTO uploadProductImage(Long id, MultipartFile file) {
        Product product = findProductById(id);

        if (file == null || file.isEmpty()) {
            return productMapper.toDto(product);
        }

        try {
            Path uploadsDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
            }

            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String ext = "";
            int idx = original.lastIndexOf('.');
            if (idx >= 0) ext = original.substring(idx);

            String filename = UUID.randomUUID() + ext;
            Path target = uploadsDir.resolve(filename);
            Files.copy(file.getInputStream(), target);

            String relative = "/uploads/" + filename;
            product.setImageUrl(relative);
            Product saved = productRepository.save(product);
            return productMapper.toDto(saved);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}

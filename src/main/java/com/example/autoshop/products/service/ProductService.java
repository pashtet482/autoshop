package com.example.autoshop.products.service;

import com.example.autoshop.products.ProductMapper;
import com.example.autoshop.products.dto.ProductDTO;
import com.example.autoshop.products.model.Product;
import com.example.autoshop.products.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

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
}

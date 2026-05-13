package com.example.autoshop.products.controller;

import com.example.autoshop.products.dto.CreateProductDTO;
import com.example.autoshop.products.dto.ProductDTO;
import com.example.autoshop.products.dto.ProductSearchFilterDTO;
import com.example.autoshop.products.dto.UpdateProductDTO;
import com.example.autoshop.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/products/search")
    public Page<ProductDTO> searchProducts(@RequestBody ProductSearchFilterDTO filter,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @RequestParam(defaultValue = "id") String sortBy,
                                           @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return productService.getFilteredProducts(
                filter,
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @GetMapping("/products")
    public Page<ProductDTO> getProducts(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "id") String sortBy,
                                        @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return productService.getFilteredProducts(
                new ProductSearchFilterDTO(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable("id") Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(dto));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody UpdateProductDTO dto,
                              @PathVariable("id") Long id){
        return ResponseEntity.ok(productService.updateProduct(dto, id));
    }

    @PostMapping("/products/{id}/image")
    public ResponseEntity<ProductDTO> uploadProductImage(@PathVariable("id") Long id,
                                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(productService.uploadProductImage(id, file));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.status(204).build();
    }
}

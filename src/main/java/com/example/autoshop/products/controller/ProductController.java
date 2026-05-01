package com.example.autoshop.products.controller;

import com.example.autoshop.products.dto.GetProductsList;
import com.example.autoshop.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<GetProductsList>> getAllProducts(){
        return ResponseEntity.ok(productService.getAllProducts());
    }
}

package com.example.autoshop.products.service;

import com.example.autoshop.products.dto.GetProductsList;
import com.example.autoshop.products.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<GetProductsList> getAllProducts(){
        return productRepository.findBy();
    }
}

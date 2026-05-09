package com.example.autoshop.products.service;

import com.example.autoshop.products.dto.CategoryDTO;
import com.example.autoshop.products.dto.InputCategoryDto;
import com.example.autoshop.products.mapper.CategoryMapper;
import com.example.autoshop.products.model.Category;
import com.example.autoshop.products.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryDTO createCategory(InputCategoryDto dto){
        Category category = categoryMapper.toEntity(dto);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(savedCategory);
    }

    public CategoryDTO getCategoryById(Long id){
        Category category = findCategoryById(id);
        return categoryMapper.toDto(category);
    }

    private @NonNull Category findCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with id: " + id + " not found"));
    }

    public CategoryDTO updateCategory(@NonNull Long id, InputCategoryDto dto){
        Category category = findCategoryById(id);
        categoryMapper.updateProduct(dto, category);

        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(updatedCategory);
    }

    public void deleteCategory(Long id){
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }
}

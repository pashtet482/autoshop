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

import java.util.List;

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
        return categoryMapper.toDto(findCategoryById(id));
    }

    private @NonNull Category findCategoryById(Long id){
        return categoryRepository.findById(id)
                .filter(Category::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Category with id: " + id + " not found"));
    }

    public CategoryDTO updateCategory(@NonNull Long id, InputCategoryDto dto){
        Category category = findCategoryById(id);
        categoryMapper.updateCategory(dto, category);

        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toDto(updatedCategory);
    }

    public void deleteCategory(Long id){
        Category category = findCategoryById(id);
        category.markDeleted();
        categoryRepository.save(category);
    }

    public List<CategoryDTO> getAllCategories(){
        return categoryRepository.findAll().stream()
                .filter(Category::isActive)
                .map(categoryMapper::toDto)
                .toList();
    }
}

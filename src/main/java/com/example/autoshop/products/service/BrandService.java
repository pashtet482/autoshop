package com.example.autoshop.products.service;

import com.example.autoshop.products.dto.BrandDTO;
import com.example.autoshop.products.dto.InputBrandDTO;
import com.example.autoshop.products.mapper.BrandMapper;
import com.example.autoshop.products.model.Brand;
import com.example.autoshop.products.repository.BrandRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    private @NonNull Brand findBrandById(Long id){
       return brandRepository.findById(id)
               .filter(Brand::isActive)
               .orElseThrow(() -> (new EntityNotFoundException("Brand with id: " + id + " not found")));
    }

    public BrandDTO createBrand(InputBrandDTO dto){
        Brand brand = brandMapper.toEntity(dto);
        Brand savedBrand = brandRepository.save(brand);
        return brandMapper.toDto(savedBrand);
    }

    public List<BrandDTO> getAllBrands(){
        return brandRepository.findAll().stream()
                .filter(Brand::isActive)
                .map(brandMapper::toDto)
                .toList();
    }

    public BrandDTO getBrandById(Long id){
        return brandMapper.toDto(findBrandById(id));
    }

    public BrandDTO updateBrand(Long id, InputBrandDTO dto){
        Brand brand = findBrandById(id);
        brandMapper.updateBrand(dto, brand);

        Brand updatedBrand = brandRepository.save(brand);

        return brandMapper.toDto(updatedBrand);
    }

    public void deleteBrand(Long id){
        Brand brand = findBrandById(id);
        brand.markDeleted();
        brandRepository.save(brand);
    }
}

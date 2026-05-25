package com.example.autoshop.supply.service;

import com.example.autoshop.supply.dto.InputSupplierDTO;
import com.example.autoshop.supply.dto.SupplierDTO;
import com.example.autoshop.supply.mapper.SupplierMapper;
import com.example.autoshop.supply.model.Supplier;
import com.example.autoshop.supply.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    private @NonNull Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .filter(Supplier::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Supplier with id: " + id + " not found"));
    }

    public SupplierDTO createSupplier(InputSupplierDTO dto) {
        Supplier supplier = supplierMapper.toEntity(dto);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toDto(savedSupplier);
    }

    public List<SupplierDTO> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .filter(Supplier::isActive)
                .map(supplierMapper::toDto)
                .toList();
    }

    public SupplierDTO getSupplierById(Long id) {
        return supplierMapper.toDto(findSupplierById(id));
    }

    public SupplierDTO updateSupplier(Long id, InputSupplierDTO dto) {
        Supplier supplier = findSupplierById(id);
        supplierMapper.updateSupplier(dto, supplier);

        Supplier updatedSupplier = supplierRepository.save(supplier);

        return supplierMapper.toDto(updatedSupplier);
    }

    public void deleteSupplier(Long id) {
        Supplier supplier = findSupplierById(id);
        supplier.markDeleted();
        supplierRepository.save(supplier);
    }
}

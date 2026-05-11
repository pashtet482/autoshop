package com.example.autoshop.products.service;

import com.example.autoshop.products.dto.InputWarehouseDTO;
import com.example.autoshop.products.dto.WarehouseDTO;
import com.example.autoshop.products.mapper.WarehouseMapper;
import com.example.autoshop.products.repository.WarehouseRepository;
import com.example.autoshop.supply.model.Warehouse;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    private @NonNull Warehouse findWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse with id: " + id + " not found"));
    }

    public WarehouseDTO createWarehouse(InputWarehouseDTO dto) {
        Warehouse warehouse = warehouseMapper.toEntity(dto);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return warehouseMapper.toDto(savedWarehouse);
    }

    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(warehouseMapper::toDto)
                .toList();
    }

    public WarehouseDTO getWarehouseById(Long id) {
        return warehouseMapper.toDto(findWarehouseById(id));
    }

    public WarehouseDTO updateWarehouse(Long id, InputWarehouseDTO dto) {
        Warehouse warehouse = findWarehouseById(id);
        warehouseMapper.updateWarehouse(dto, warehouse);

        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);

        return warehouseMapper.toDto(updatedWarehouse);
    }

    public void deleteWarehouse(Long id) {
        warehouseRepository.delete(findWarehouseById(id));
    }
}

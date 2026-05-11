package com.example.autoshop.supply.controller;

import com.example.autoshop.supply.dto.InputSupplierDTO;
import com.example.autoshop.supply.dto.SupplierDTO;
import com.example.autoshop.supply.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SupplierController {
    private final SupplierService supplierService;

    @PostMapping("/suppliers")
    public ResponseEntity<SupplierDTO> createSupplier(@RequestBody InputSupplierDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(dto));
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable("id") Long id, @RequestBody InputSupplierDTO dto) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, dto));
    }

    @DeleteMapping("/suppliers/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable("id") Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suppliers")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }
}

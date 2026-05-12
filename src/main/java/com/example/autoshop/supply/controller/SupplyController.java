package com.example.autoshop.supply.controller;

import com.example.autoshop.supply.dto.InputSupplyDTO;
import com.example.autoshop.supply.dto.SupplyDTO;
import com.example.autoshop.supply.service.SupplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService supplyService;

    @PostMapping
    public ResponseEntity<SupplyDTO> createSupply(
            @Valid @RequestBody InputSupplyDTO dto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(supplyService.createSupply(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplyDTO> getSupplyById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                supplyService.getSupplyById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<SupplyDTO>> getAllSupplies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(
                supplyService.getAllSupplies(
                        page,
                        size,
                        sortBy,
                        sortDirection
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplyDTO> updateSupply(
            @PathVariable Long id,
            @Valid @RequestBody InputSupplyDTO dto
    ) {
        return ResponseEntity.ok(
                supplyService.updateSupply(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupply(
            @PathVariable Long id
    ) {
        supplyService.deleteSupply(id);

        return ResponseEntity.noContent().build();
    }
}
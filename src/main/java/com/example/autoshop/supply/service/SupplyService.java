package com.example.autoshop.supply.service;

import com.example.autoshop.products.model.Product;
import com.example.autoshop.products.model.ProductStock;
import com.example.autoshop.products.repository.ProductRepository;
import com.example.autoshop.products.repository.ProductStockRepository;
import com.example.autoshop.supply.dto.InputSupplyItemDTO;
import com.example.autoshop.supply.dto.InputSupplyDTO;
import com.example.autoshop.supply.dto.SupplyDTO;
import com.example.autoshop.supply.mapper.SupplyItemMapper;
import com.example.autoshop.supply.mapper.SupplyMapper;
import com.example.autoshop.supply.model.Supplier;
import com.example.autoshop.supply.model.Supply;
import com.example.autoshop.supply.model.SupplyItem;
import com.example.autoshop.supply.model.Warehouse;
import com.example.autoshop.supply.repository.SupplierRepository;
import com.example.autoshop.supply.repository.SupplyItemsRepository;
import com.example.autoshop.supply.repository.SupplyRepository;
import com.example.autoshop.supply.repository.WarehouseRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class SupplyService {

    private final SupplyMapper supplyMapper;
    private final SupplyItemMapper supplyItemMapper;
    private final SupplyRepository supplyRepository;
    private final SupplyItemsRepository supplyItemsRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ProductStockRepository productStockRepository;

    @Transactional
    public SupplyDTO createSupply(InputSupplyDTO dto) {

        Supply supply = supplyMapper.toEntity(dto);

        supply.setSupplier(
                findSupplierById(dto.supplierId())
        );

        List<SupplyItem> items = dto.items().stream()
                .map(supplyItemMapper::toEntity)
                .toList();

        Supply savedSupply = supplyRepository.save(supply);

        for (int i = 0; i < items.size(); i++) {

            SupplyItem item = items.get(i);
            InputSupplyItemDTO itemDto = dto.items().get(i);

            item.setSupply(savedSupply);
            item.setProduct(findProductById(itemDto.productId()));
            item.setWarehouse(findWarehouseById(itemDto.warehouseId()));

            ProductStock stock = productStockRepository
                    .findByProductAndWarehouse(
                            item.getProduct(),
                            item.getWarehouse()
                    )
                    .orElseGet(() -> {
                        ProductStock newStock = new ProductStock();
                        newStock.setProduct(item.getProduct());
                        newStock.setWarehouse(item.getWarehouse());
                        newStock.setQuantity(0);
                        return newStock;
                    });

            stock.setQuantity(
                    stock.getQuantity() + item.getQuantity()
            );

            productStockRepository.save(stock);
        }

        supplyItemsRepository.saveAll(items);

        savedSupply.setItems(items);

        return supplyMapper.toDto(savedSupply);
    }

    @Transactional(readOnly = true)
    public SupplyDTO getSupplyById(Long id) {

        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supply not found"));

        return supplyMapper.toDto(supply);
    }

    @Transactional(readOnly = true)
    public Page<SupplyDTO> getAllSupplies(
            int page,
            int size,
            String sortBy,
            @NonNull String sortDirection
    ) {

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return supplyRepository.findAll(pageable)
                .map(supplyMapper::toDto);
    }

    public SupplyDTO updateSupply(Long id, InputSupplyDTO dto) {

        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supply not found"));

        supplyMapper.updateSupply(dto, supply);

        supply.setSupplier(
                findSupplierById(dto.supplierId())
        );

        Supply updatedSupply = supplyRepository.save(supply);

        return supplyMapper.toDto(updatedSupply);
    }

    public void deleteSupply(Long id) {

        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supply not found"));

        supplyRepository.delete(supply);
    }

    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    private Warehouse findWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
    }
}

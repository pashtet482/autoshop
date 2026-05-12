package com.example.autoshop.supply.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record SupplyDTO(
        Long id,
        Long supplierId,
        OffsetDateTime dateOfSupply,
        List<SupplyItemDTO> items
) {
}

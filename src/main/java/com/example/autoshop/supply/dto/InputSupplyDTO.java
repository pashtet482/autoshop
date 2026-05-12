package com.example.autoshop.supply.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record InputSupplyDTO(
        Long supplierId,
        OffsetDateTime dateOfSupply,
        List<InputSupplyItemDTO> items
) {
}

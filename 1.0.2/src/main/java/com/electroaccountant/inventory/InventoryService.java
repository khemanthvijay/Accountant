package com.electroaccountant.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.electroaccountant.shared.ApiException;
import com.electroaccountant.shared.Money;
import com.electroaccountant.shared.Quantity;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) { this.inventoryRepository = inventoryRepository; }

    @Transactional
    public ItemResponse createItem(CreateItemRequest request) {
        long opening = Quantity.toScaled(request.openingQuantity());
        long reorder = request.reorderLevel() == null ? 0 : Quantity.toScaled(request.reorderLevel());
        long openingRate = rate(request.openingRate());
        long purchaseRate = rate(request.defaultPurchaseRate());
        long salesRate = rate(request.defaultSalesRate());
        String itemId = UUID.randomUUID().toString();
        try {
            inventoryRepository.insertItem(itemId, request.sku().trim(), request.name().trim(), blankToNull(request.description()),
                    blankToNull(request.alias()), blankToNull(request.category()), request.unit().trim(), opening, reorder, openingRate, purchaseRate, salesRate);
        } catch (org.springframework.dao.DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "An item with this SKU already exists");
        }
        inventoryRepository.insertMovement(UUID.randomUUID().toString(), itemId, LocalDate.now(), "OPENING", opening, "ITEM", itemId);
        return new ItemResponse(itemId, request.sku().trim(), request.name().trim(), blankToNull(request.description()), blankToNull(request.alias()),
                blankToNull(request.category()), request.unit().trim(), Quantity.fromScaled(opening), Quantity.fromScaled(reorder),
                Money.fromMinor(openingRate), Money.fromMinor(purchaseRate), Money.fromMinor(salesRate), opening <= reorder);
    }

    public List<ItemResponse> stock() {
        return inventoryRepository.findAllStock().stream().map(row -> new ItemResponse(row.id(), row.sku(), row.name(), row.description(),
                row.alias(), row.category(), row.unit(), Quantity.fromScaled(row.availableQuantityScaled()), Quantity.fromScaled(row.reorderLevelScaled()),
                Money.fromMinor(row.openingRateMinor()), Money.fromMinor(row.defaultPurchaseRateMinor()), Money.fromMinor(row.defaultSalesRateMinor()),
                row.availableQuantityScaled() <= row.reorderLevelScaled())).toList();
    }

    @Transactional
    public ItemResponse updateItem(String itemId, UpdateItemRequest request) {
        ItemResponse existing = item(itemId);
        long reorder = request.reorderLevel() == null ? 0 : Quantity.toScaled(request.reorderLevel());
        long purchaseRate = rate(request.defaultPurchaseRate());
        long salesRate = rate(request.defaultSalesRate());
        try {
            inventoryRepository.updateItem(itemId, request.sku().trim(), request.name().trim(), blankToNull(request.description()),
                    blankToNull(request.alias()), blankToNull(request.category()), request.unit().trim(), reorder, purchaseRate, salesRate);
        } catch (org.springframework.dao.DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "An item with this SKU already exists");
        }
        return new ItemResponse(existing.id(), request.sku().trim(), request.name().trim(), blankToNull(request.description()), blankToNull(request.alias()),
                blankToNull(request.category()), request.unit().trim(), existing.availableQuantity(), Quantity.fromScaled(reorder), existing.openingRate(),
                Money.fromMinor(purchaseRate), Money.fromMinor(salesRate), existing.availableQuantity().compareTo(Quantity.fromScaled(reorder)) <= 0);
    }

    public ItemResponse item(String itemId) {
        return stock().stream().filter(item -> item.id().equals(itemId)).findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    public List<StockMovementResponse> movements(String itemId) {
        if (inventoryRepository.findActiveItem(itemId).isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Item not found");
        }
        return inventoryRepository.findMovements(itemId).stream().map(row -> new StockMovementResponse(row.id(), row.date(), row.type(),
                Quantity.fromScaled(row.quantityDeltaScaled()), row.sourceType(), row.sourceId())).toList();
    }

    public InventoryRepository.ItemRow requiredActiveItem(String itemId) {
        return inventoryRepository.findActiveItem(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Item not found or inactive: " + itemId));
    }

    public long availableQuantity(String itemId) { return inventoryRepository.availableQuantity(itemId); }

    public void recordMovement(String itemId, LocalDate date, String type, long quantityDelta, String sourceType, String sourceId) {
        inventoryRepository.insertMovement(UUID.randomUUID().toString(), itemId, date, type, quantityDelta, sourceType, sourceId);
    }

    public void removeMovementsForSource(String sourceId) { inventoryRepository.deleteMovementsBySource(sourceId); }

    private long rate(BigDecimal value) { return value == null ? 0 : Money.toMinor(value); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}

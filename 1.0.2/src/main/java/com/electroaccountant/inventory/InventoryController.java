package com.electroaccountant.inventory;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService; }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    ItemResponse createItem(@Valid @RequestBody CreateItemRequest request) { return inventoryService.createItem(request); }

    @GetMapping("/items/{itemId}")
    ItemResponse item(@PathVariable String itemId) { return inventoryService.item(itemId); }

    @PutMapping("/items/{itemId}")
    ItemResponse updateItem(@PathVariable String itemId, @Valid @RequestBody UpdateItemRequest request) { return inventoryService.updateItem(itemId, request); }

    @GetMapping("/inventory/stock")
    List<ItemResponse> stock() { return inventoryService.stock(); }

    @GetMapping("/inventory/items/{itemId}/movements")
    List<StockMovementResponse> movements(@PathVariable String itemId) { return inventoryService.movements(itemId); }
}

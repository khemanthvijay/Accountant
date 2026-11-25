package com.electroaccountant.inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryRepository {
    private final JdbcTemplate jdbc;

    public InventoryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void insertItem(String id, String sku, String name, String description, String alias, String category, String unit,
                           long opening, long reorder, long openingRate, long purchaseRate, long salesRate) {
        jdbc.update("INSERT INTO inventory_item (id, sku, name, description, alias, category, unit, opening_quantity_scaled, reorder_level_scaled, opening_rate_minor, default_purchase_rate_minor, default_sales_rate_minor, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, sku, name, description, alias, category, unit, opening, reorder, openingRate, purchaseRate, salesRate, Instant.now().toString());
    }

    public void updateItem(String id, String sku, String name, String description, String alias, String category, String unit,
                           long reorder, long purchaseRate, long salesRate) {
        jdbc.update("UPDATE inventory_item SET sku = ?, name = ?, description = ?, alias = ?, category = ?, unit = ?, reorder_level_scaled = ?, default_purchase_rate_minor = ?, default_sales_rate_minor = ? WHERE id = ? AND active = 1",
                sku, name, description, alias, category, unit, reorder, purchaseRate, salesRate, id);
    }

    public Optional<ItemRow> findActiveItem(String id) {
        return jdbc.query("SELECT id, sku, name, unit FROM inventory_item WHERE id = ? AND active = 1", itemMapper, id)
                .stream().findFirst();
    }

    public long availableQuantity(String itemId) {
        Long result = jdbc.queryForObject("SELECT COALESCE(SUM(quantity_delta_scaled), 0) FROM stock_movement WHERE item_id = ?", Long.class, itemId);
        return result == null ? 0L : result;
    }

    public void insertMovement(String id, String itemId, LocalDate date, String type, long quantityDelta, String sourceType, String sourceId) {
        jdbc.update("INSERT INTO stock_movement (id, item_id, movement_date, movement_type, quantity_delta_scaled, source_type, source_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, itemId, date.toString(), type, quantityDelta, sourceType, sourceId, Instant.now().toString());
    }

    public void deleteMovementsBySource(String sourceId) {
        jdbc.update("DELETE FROM stock_movement WHERE source_id = ?", sourceId);
    }

    public List<ItemStockRow> findAllStock() {
        return jdbc.query("""
                SELECT i.id, i.sku, i.name, i.description, i.alias, i.category, i.unit, i.reorder_level_scaled,
                       i.opening_rate_minor, i.default_purchase_rate_minor, i.default_sales_rate_minor,
                       COALESCE(SUM(m.quantity_delta_scaled), 0) AS available_quantity_scaled
                FROM inventory_item i
                LEFT JOIN stock_movement m ON m.item_id = i.id
                WHERE i.active = 1
                GROUP BY i.id, i.sku, i.name, i.description, i.alias, i.category, i.unit, i.reorder_level_scaled,
                         i.opening_rate_minor, i.default_purchase_rate_minor, i.default_sales_rate_minor
                ORDER BY i.name
                """, stockMapper);
    }

    public List<StockMovementRow> findMovements(String itemId) {
        return jdbc.query("SELECT id, movement_date, movement_type, quantity_delta_scaled, source_type, source_id FROM stock_movement WHERE item_id = ? ORDER BY movement_date DESC, created_at DESC", movementMapper, itemId);
    }

    private final RowMapper<ItemRow> itemMapper = (resultSet, row) -> new ItemRow(
            resultSet.getString("id"), resultSet.getString("sku"), resultSet.getString("name"), resultSet.getString("unit"));
    private final RowMapper<ItemStockRow> stockMapper = (resultSet, row) -> new ItemStockRow(
            resultSet.getString("id"), resultSet.getString("sku"), resultSet.getString("name"), resultSet.getString("description"),
            resultSet.getString("alias"), resultSet.getString("category"), resultSet.getString("unit"),
            resultSet.getLong("available_quantity_scaled"), resultSet.getLong("reorder_level_scaled"), resultSet.getLong("opening_rate_minor"),
            resultSet.getLong("default_purchase_rate_minor"), resultSet.getLong("default_sales_rate_minor"));
    private final RowMapper<StockMovementRow> movementMapper = (resultSet, row) -> new StockMovementRow(
            resultSet.getString("id"), LocalDate.parse(resultSet.getString("movement_date")), resultSet.getString("movement_type"),
            resultSet.getLong("quantity_delta_scaled"), resultSet.getString("source_type"), resultSet.getString("source_id"));

    public record ItemRow(String id, String sku, String name, String unit) { }
    public record ItemStockRow(String id, String sku, String name, String description, String alias, String category, String unit,
                               long availableQuantityScaled, long reorderLevelScaled, long openingRateMinor,
                               long defaultPurchaseRateMinor, long defaultSalesRateMinor) { }
    public record StockMovementRow(String id, LocalDate date, String type, long quantityDeltaScaled, String sourceType, String sourceId) { }
}

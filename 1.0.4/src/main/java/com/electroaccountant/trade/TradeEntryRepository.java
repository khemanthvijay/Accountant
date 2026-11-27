package com.electroaccountant.trade;

import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TradeEntryRepository {
    private final JdbcTemplate jdbc;
    public TradeEntryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void insertHeader(String type, String id, String partyId, LocalDate date, String partyName, String paymentType, String documentNumber, String notes, long total) {
        String table = tableFor(type);
        String partyIdColumn = type.equals("PURCHASE") ? "supplier_id" : "customer_id";
        String partyColumn = type.equals("PURCHASE") ? "supplier_name" : "customer_name";
        String documentColumn = type.equals("PURCHASE") ? "supplier_invoice_number" : "invoice_number";
        if (type.equals("SALE")) {
            String legacyPaymentType = paymentType.equals("BANK") ? "CASH" : paymentType;
            jdbc.update("INSERT INTO sales_entry (id, customer_id, entry_date, customer_name, invoice_number, payment_type, payment_type_v2, notes, total_minor, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    id, partyId, date.toString(), partyName, documentNumber, legacyPaymentType, paymentType, notes, total, Instant.now().toString());
        } else {
            jdbc.update("INSERT INTO " + table + " (id, " + partyIdColumn + ", entry_date, " + partyColumn + ", " + documentColumn + ", payment_type, notes, total_minor, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    id, partyId, date.toString(), partyName, documentNumber, paymentType, notes, total, Instant.now().toString());
        }
    }

    public void updateHeader(String type, String id, String partyId, LocalDate date, String partyName, String paymentType, String documentNumber, String notes, long total) {
        String table = tableFor(type);
        String partyIdColumn = type.equals("PURCHASE") ? "supplier_id" : "customer_id";
        String partyColumn = type.equals("PURCHASE") ? "supplier_name" : "customer_name";
        String documentColumn = type.equals("PURCHASE") ? "supplier_invoice_number" : "invoice_number";
        if (type.equals("SALE")) {
            String legacyPaymentType = paymentType.equals("BANK") ? "CASH" : paymentType;
            jdbc.update("UPDATE sales_entry SET customer_id = ?, entry_date = ?, customer_name = ?, invoice_number = ?, payment_type = ?, payment_type_v2 = ?, notes = ?, total_minor = ? WHERE id = ?",
                    partyId, date.toString(), partyName, documentNumber, legacyPaymentType, paymentType, notes, total, id);
        } else {
            jdbc.update("UPDATE " + table + " SET " + partyIdColumn + " = ?, entry_date = ?, " + partyColumn + " = ?, " + documentColumn + " = ?, payment_type = ?, notes = ?, total_minor = ? WHERE id = ?",
                    partyId, date.toString(), partyName, documentNumber, paymentType, notes, total, id);
        }
    }

    public void insertLine(String type, String lineId, String entryId, String itemId, String itemName, long quantity, long unitAmount, long lineTotal) {
        String table = type.equals("PURCHASE") ? "purchase_entry_line" : "sales_entry_line";
        String foreignKey = type.equals("PURCHASE") ? "purchase_entry_id" : "sales_entry_id";
        String amountColumn = type.equals("PURCHASE") ? "unit_cost_minor" : "unit_price_minor";
        jdbc.update("INSERT INTO " + table + " (id, " + foreignKey + ", item_id, item_name_snapshot, quantity_scaled, " + amountColumn + ", line_total_minor) VALUES (?, ?, ?, ?, ?, ?, ?)",
                lineId, entryId, itemId, itemName, quantity, unitAmount, lineTotal);
    }

    public List<EntryRow> findHeaders(String type, LocalDate date) {
        String table = tableFor(type);
        String partyIdColumn = type.equals("PURCHASE") ? "supplier_id" : "customer_id";
        String partyColumn = type.equals("PURCHASE") ? "supplier_name" : "customer_name";
        String documentColumn = type.equals("PURCHASE") ? "supplier_invoice_number" : "invoice_number";
        String paymentSelect = type.equals("SALE") ? "payment_type_v2" : "payment_type";
        String sql = "SELECT id, " + partyIdColumn + " AS party_id, entry_date, " + partyColumn + " AS party_name, " + paymentSelect + ", " + documentColumn + " AS document_number, notes, total_minor FROM " + table;
        return date == null ? jdbc.query(sql + " ORDER BY entry_date DESC, created_at DESC", headerMapper)
                : jdbc.query(sql + " WHERE entry_date = ? ORDER BY created_at DESC", headerMapper, date.toString());
    }

    public java.util.Optional<EntryRow> findHeader(String type, String id) {
        String table = tableFor(type);
        String partyIdColumn = type.equals("PURCHASE") ? "supplier_id" : "customer_id";
        String partyColumn = type.equals("PURCHASE") ? "supplier_name" : "customer_name";
        String documentColumn = type.equals("PURCHASE") ? "supplier_invoice_number" : "invoice_number";
        String paymentSelect = type.equals("SALE") ? "payment_type_v2" : "payment_type";
        return jdbc.query("SELECT id, " + partyIdColumn + " AS party_id, entry_date, " + partyColumn + " AS party_name, " + paymentSelect + ", " + documentColumn + " AS document_number, notes, total_minor FROM " + table + " WHERE id = ?", headerMapper, id).stream().findFirst();
    }

    public void deleteLines(String type, String entryId) {
        String table = type.equals("PURCHASE") ? "purchase_entry_line" : "sales_entry_line";
        String foreignKey = type.equals("PURCHASE") ? "purchase_entry_id" : "sales_entry_id";
        jdbc.update("DELETE FROM " + table + " WHERE " + foreignKey + " = ?", entryId);
    }

    public List<LineRow> findLines(String type, String id) {
        String table = type.equals("PURCHASE") ? "purchase_entry_line" : "sales_entry_line";
        String foreignKey = type.equals("PURCHASE") ? "purchase_entry_id" : "sales_entry_id";
        String amountColumn = type.equals("PURCHASE") ? "unit_cost_minor" : "unit_price_minor";
        return jdbc.query("SELECT item_id, item_name_snapshot, quantity_scaled, " + amountColumn + " AS unit_amount_minor, line_total_minor FROM " + table + " WHERE " + foreignKey + " = ? ORDER BY rowid", lineMapper, id);
    }

    private String tableFor(String type) { return type.equals("PURCHASE") ? "purchase_entry" : "sales_entry"; }
    private final RowMapper<EntryRow> headerMapper = (rs, row) -> new EntryRow(rs.getString("id"), rs.getString("party_id"), LocalDate.parse(rs.getString("entry_date")),
            rs.getString("party_name"), rs.getString("payment_type"), rs.getString("document_number"), rs.getString("notes"), rs.getLong("total_minor"));
    private final RowMapper<LineRow> lineMapper = (rs, row) -> new LineRow(rs.getString("item_id"), rs.getString("item_name_snapshot"),
            rs.getLong("quantity_scaled"), rs.getLong("unit_amount_minor"), rs.getLong("line_total_minor"));

    public record EntryRow(String id, String partyId, LocalDate date, String partyName, String paymentType, String documentNumber, String notes, long totalMinor) { }
    public record LineRow(String itemId, String itemName, long quantityScaled, long unitAmountMinor, long lineTotalMinor) { }
}

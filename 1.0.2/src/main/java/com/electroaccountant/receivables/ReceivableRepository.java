package com.electroaccountant.receivables;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReceivableRepository {
    private final JdbcTemplate jdbc;
    public ReceivableRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public long creditSales(String customerId) { Long value = jdbc.queryForObject("SELECT COALESCE(SUM(total_minor), 0) FROM sales_entry WHERE customer_id = ? AND payment_type = 'CREDIT'", Long.class, customerId); return value == null ? 0 : value; }
    public long receipts(String customerId) { Long value = jdbc.queryForObject("SELECT COALESCE(SUM(amount_minor), 0) FROM customer_receipt WHERE customer_id = ?", Long.class, customerId); return value == null ? 0 : value; }
    public void insert(String id, String customerId, LocalDate date, long amount, ReceiptMethod method, String reference, String notes) { jdbc.update("INSERT INTO customer_receipt (id, customer_id, receipt_date, amount_minor, payment_method, reference_number, notes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", id, customerId, date.toString(), amount, method.name(), reference, notes, Instant.now().toString()); }
    public List<ReceiptRow> findAll() { return jdbc.query("SELECT r.id, r.customer_id, p.name AS customer_name, r.receipt_date, r.amount_minor, r.payment_method, r.reference_number, r.notes FROM customer_receipt r JOIN party p ON p.id = r.customer_id ORDER BY r.receipt_date DESC, r.created_at DESC", (rs, row) -> row(rs)); }
    public List<ReceiptRow> findByCustomer(String customerId) { return jdbc.query("SELECT r.id, r.customer_id, p.name AS customer_name, r.receipt_date, r.amount_minor, r.payment_method, r.reference_number, r.notes FROM customer_receipt r JOIN party p ON p.id = r.customer_id WHERE r.customer_id = ? ORDER BY r.receipt_date DESC, r.created_at DESC", (rs, row) -> row(rs), customerId); }
    private ReceiptRow row(java.sql.ResultSet rs) throws java.sql.SQLException { return new ReceiptRow(rs.getString("id"), rs.getString("customer_id"), rs.getString("customer_name"), LocalDate.parse(rs.getString("receipt_date")), rs.getLong("amount_minor"), ReceiptMethod.valueOf(rs.getString("payment_method")), rs.getString("reference_number"), rs.getString("notes")); }
    public record ReceiptRow(String id, String customerId, String customerName, LocalDate receiptDate, long amountMinor, ReceiptMethod method, String referenceNumber, String notes) { }
}

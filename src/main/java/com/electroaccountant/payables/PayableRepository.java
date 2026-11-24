package com.electroaccountant.payables;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.electroaccountant.receivables.ReceiptMethod;

@Repository
public class PayableRepository {
    private final JdbcTemplate jdbc;
    public PayableRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public long creditPurchases(String supplierId) { return total("SELECT COALESCE(SUM(total_minor),0) FROM purchase_entry WHERE supplier_id=? AND payment_type='CREDIT'", supplierId); }
    public long creditExpenses(String supplierId) { return total("SELECT COALESCE(SUM(amount_minor),0) FROM expense_entry WHERE supplier_id=? AND payment_type='CREDIT'", supplierId); }
    public long payments(String supplierId) { return total("SELECT COALESCE(SUM(amount_minor),0) FROM supplier_payment WHERE supplier_id=?", supplierId); }
    private long total(String sql, String id) { Long value = jdbc.queryForObject(sql, Long.class, id); return value == null ? 0 : value; }
    public void insert(String id, String supplierId, LocalDate date, long amount, ReceiptMethod method, String reference, String notes) {
        jdbc.update("INSERT INTO supplier_payment (id,supplier_id,payment_date,amount_minor,payment_method,reference_number,notes,created_at) VALUES (?,?,?,?,?,?,?,?)", id,supplierId,date.toString(),amount,method.name(),reference,notes, Instant.now().toString());
    }
    public List<Row> findAll() { return jdbc.query("SELECT sp.id,sp.supplier_id,p.name supplier_name,sp.payment_date,sp.amount_minor,sp.payment_method,sp.reference_number,sp.notes FROM supplier_payment sp JOIN party p ON p.id=sp.supplier_id ORDER BY sp.payment_date DESC,sp.created_at DESC", (rs,n)->row(rs)); }
    private Row row(java.sql.ResultSet rs) throws java.sql.SQLException { return new Row(rs.getString("id"),rs.getString("supplier_id"),rs.getString("supplier_name"),LocalDate.parse(rs.getString("payment_date")),rs.getLong("amount_minor"),ReceiptMethod.valueOf(rs.getString("payment_method")),rs.getString("reference_number"),rs.getString("notes")); }
    public record Row(String id,String supplierId,String supplierName,LocalDate date,long amountMinor,ReceiptMethod method,String reference,String notes) { }
}

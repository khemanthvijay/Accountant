package com.electroaccountant.transactions;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.electroaccountant.shared.Money;

@Service
public class TransactionService {
    private final JdbcTemplate jdbc;
    public TransactionService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<TransactionResponse> all(LocalDate from, LocalDate to, String partyId) {
        String sql = """
                SELECT * FROM (
                  SELECT id, 'SALE' type, entry_date transaction_date, customer_id party_id, customer_name party_name, invoice_number reference, notes, total_minor amount_minor, created_at FROM sales_entry
                  UNION ALL
                  SELECT id, 'PURCHASE' type, entry_date transaction_date, supplier_id party_id, supplier_name party_name, supplier_invoice_number reference, notes, total_minor amount_minor, created_at FROM purchase_entry
                  UNION ALL
                  SELECT r.id, 'RECEIPT' type, r.receipt_date transaction_date, r.customer_id party_id, p.name party_name, r.reference_number reference, r.notes, r.amount_minor amount_minor, r.created_at FROM customer_receipt r JOIN party p ON p.id = r.customer_id
                  UNION ALL
                  SELECT sp.id, 'SUPPLIER_PAYMENT' type, sp.payment_date transaction_date, sp.supplier_id party_id, p.name party_name, sp.reference_number reference, sp.notes, sp.amount_minor amount_minor, sp.created_at FROM supplier_payment sp JOIN party p ON p.id = sp.supplier_id
                  UNION ALL
                  SELECT e.id, 'EXPENSE' type, e.expense_date transaction_date, e.supplier_id party_id, p.name party_name, e.reference_number reference, e.description || CASE WHEN e.notes IS NULL OR e.notes = '' THEN '' ELSE ' - ' || e.notes END notes, e.amount_minor amount_minor, e.created_at FROM expense_entry e LEFT JOIN party p ON p.id = e.supplier_id
                )
                WHERE (? IS NULL OR transaction_date >= ?)
                  AND (? IS NULL OR transaction_date <= ?)
                  AND (? IS NULL OR party_id = ?)
                ORDER BY transaction_date DESC, created_at DESC
                """;
        String fromValue = from == null ? null : from.toString();
        String toValue = to == null ? null : to.toString();
        String selectedParty = partyId == null || partyId.isBlank() ? null : partyId;
        return jdbc.query(sql, (rs, row) -> new TransactionResponse(
                rs.getString("id"), rs.getString("type"), LocalDate.parse(rs.getString("transaction_date")),
                rs.getString("party_id"), rs.getString("party_name"), rs.getString("reference"), rs.getString("notes"),
                Money.fromMinor(rs.getLong("amount_minor"))),
                fromValue, fromValue, toValue, toValue, selectedParty, selectedParty);
    }
}

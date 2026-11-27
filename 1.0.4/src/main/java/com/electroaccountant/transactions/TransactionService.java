package com.electroaccountant.transactions;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public LedgerStatementResponse statement(String partyId, LocalDate from, LocalDate to) {
        if (partyId == null || partyId.isBlank()) throw new IllegalArgumentException("Party is required");
        LocalDate fromDate = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate toDate = to == null ? LocalDate.now() : to;
        String partyName = jdbc.query("SELECT name FROM party WHERE id = ? AND active = 1", (rs, row) -> rs.getString(1), partyId).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Active ledger not found"));
        List<StatementRow> rows = jdbc.query("""
                SELECT * FROM (
                  SELECT id, entry_date transaction_date, 'SALE' type, invoice_number reference, notes, total_minor debit_minor, 0 credit_minor, created_at FROM sales_entry WHERE customer_id = ?
                  UNION ALL SELECT id, receipt_date, 'RECEIPT', reference_number, notes, 0, amount_minor, created_at FROM customer_receipt WHERE customer_id = ?
                  UNION ALL SELECT id, entry_date, 'PURCHASE', supplier_invoice_number, notes, 0, total_minor, created_at FROM purchase_entry WHERE supplier_id = ?
                  UNION ALL SELECT id, payment_date, 'SUPPLIER_PAYMENT', reference_number, notes, amount_minor, 0, created_at FROM supplier_payment WHERE supplier_id = ?
                  UNION ALL SELECT id, expense_date, 'EXPENSE', reference_number, description || CASE WHEN notes IS NULL OR notes = '' THEN '' ELSE ' - ' || notes END, 0, amount_minor, created_at FROM expense_entry WHERE supplier_id = ?
                ) WHERE transaction_date <= ? ORDER BY transaction_date, created_at
                """, (rs, row) -> new StatementRow(LocalDate.parse(rs.getString("transaction_date")), rs.getString("type"), rs.getString("reference"), rs.getString("notes"), rs.getLong("debit_minor"), rs.getLong("credit_minor")), partyId, partyId, partyId, partyId, partyId, toDate.toString());
        long balance = 0, opening = 0, sales = 0, purchases = 0, received = 0, paid = 0;
        List<LedgerStatementLineResponse> lines = new ArrayList<>();
        for (StatementRow row : rows) {
            if (row.date().isBefore(fromDate)) { balance += row.debitMinor() - row.creditMinor(); opening = balance; continue; }
            balance += row.debitMinor() - row.creditMinor();
            if (row.type().equals("SALE")) sales += row.debitMinor();
            if (row.type().equals("PURCHASE")) purchases += row.creditMinor();
            if (row.type().equals("RECEIPT")) received += row.creditMinor();
            if (row.type().equals("SUPPLIER_PAYMENT")) paid += row.debitMinor();
            lines.add(new LedgerStatementLineResponse(row.date(), row.type(), row.reference(), row.notes(), Money.fromMinor(row.debitMinor()), Money.fromMinor(row.creditMinor()), Money.fromMinor(balance)));
        }
        return new LedgerStatementResponse(partyId, partyName, fromDate, toDate, Money.fromMinor(opening), Money.fromMinor(sales), Money.fromMinor(purchases), Money.fromMinor(received), Money.fromMinor(paid), Money.fromMinor(balance), lines);
    }
    private record StatementRow(LocalDate date, String type, String reference, String notes, long debitMinor, long creditMinor) { }
}

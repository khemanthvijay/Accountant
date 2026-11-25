package com.electroaccountant.journal;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.electroaccountant.receivables.ReceiptMethod;
import com.electroaccountant.shared.Money;
import com.electroaccountant.trade.PaymentType;

@Service
public class JournalService {
    private final JdbcTemplate jdbc; public JournalService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Transactional public void postPurchase(String id, LocalDate date, String supplier, long amount, PaymentType paymentType) { post("PURCHASE",id,date,"Purchase from "+supplier,List.of(debit("INVENTORY","Inventory",amount),credit(paymentType==PaymentType.CREDIT?"ACCOUNTS_PAYABLE":cashCode(ReceiptMethod.CASH),paymentType==PaymentType.CREDIT?"Accounts payable":"Cash",amount))); }
    @Transactional public void postSale(String id, LocalDate date, String customer, long saleAmount, PaymentType paymentType, long cost) { post("SALE",id,date,"Sale to "+customer,List.of(debit(paymentType==PaymentType.CREDIT?"ACCOUNTS_RECEIVABLE":"CASH",paymentType==PaymentType.CREDIT?"Accounts receivable":"Cash",saleAmount),credit("SALES","Sales",saleAmount),debit("COST_OF_GOODS_SOLD","Cost of goods sold",cost),credit("INVENTORY","Inventory",cost))); }
    @Transactional public void postCustomerReceipt(String id, LocalDate date, String customer, long amount, ReceiptMethod method) { post("CUSTOMER_RECEIPT",id,date,"Receipt from "+customer,List.of(debit(cashCode(method),cashName(method),amount),credit("ACCOUNTS_RECEIVABLE","Accounts receivable",amount))); }
    @Transactional public void postSupplierPayment(String id, LocalDate date, String supplier, long amount, ReceiptMethod method) { post("SUPPLIER_PAYMENT",id,date,"Payment to "+supplier,List.of(debit("ACCOUNTS_PAYABLE","Accounts payable",amount),credit(cashCode(method),cashName(method),amount))); }
    @Transactional public void postExpense(String id, LocalDate date, String expenseType, String description, long amount, PaymentType paymentType, ReceiptMethod method) { String code="EXPENSE_"+expenseType; String name=expenseType.substring(0,1)+expenseType.substring(1).toLowerCase().replace('_',' ') + " expense"; post("EXPENSE",id,date,description,List.of(debit(code,name,amount),credit(paymentType==PaymentType.CREDIT?"ACCOUNTS_PAYABLE":cashCode(method),paymentType==PaymentType.CREDIT?"Accounts payable":cashName(method),amount))); }
    public List<JournalEntryResponse> entries(LocalDate from, LocalDate to) { String sql="SELECT id,entry_date,source_type,narration FROM journal_entry WHERE (? IS NULL OR entry_date>=?) AND (? IS NULL OR entry_date<=?) ORDER BY entry_date DESC,created_at DESC"; return jdbc.query(sql,(rs,n)->new Header(rs.getString("id"),LocalDate.parse(rs.getString("entry_date")),rs.getString("source_type"),rs.getString("narration")),from==null?null:from.toString(),from==null?null:from.toString(),to==null?null:to.toString(),to==null?null:to.toString()).stream().map(h->new JournalEntryResponse(h.id,h.date,h.type,h.narration,lines(h.id))).toList(); }
    public List<JournalLineResponse> lines(String entryId) { return jdbc.query("SELECT account_code,account_name,debit_minor,credit_minor FROM journal_line WHERE journal_entry_id=? ORDER BY rowid",(rs,n)->new JournalLineResponse(rs.getString("account_code"),rs.getString("account_name"),Money.fromMinor(rs.getLong("debit_minor")),Money.fromMinor(rs.getLong("credit_minor"))),entryId); }
    private void post(String sourceType,String sourceId,LocalDate date,String narration,List<Line> lines) { long debit=lines.stream().mapToLong(Line::debit).sum(), credit=lines.stream().mapToLong(Line::credit).sum(); if(debit!=credit) throw new IllegalArgumentException("Journal is not balanced"); jdbc.update("DELETE FROM journal_entry WHERE source_type=? AND source_id=?",sourceType,sourceId); String id=UUID.randomUUID().toString(); jdbc.update("INSERT INTO journal_entry (id,entry_date,source_type,source_id,narration,created_at) VALUES (?,?,?,?,?,?)",id,date.toString(),sourceType,sourceId,narration,Instant.now().toString()); for(Line line:lines) if(line.debit>0||line.credit>0) jdbc.update("INSERT INTO journal_line (id,journal_entry_id,account_code,account_name,debit_minor,credit_minor) VALUES (?,?,?,?,?,?)",UUID.randomUUID().toString(),id,line.code,line.name,line.debit,line.credit); }
    private Line debit(String code,String name,long amount) { return new Line(code,name,amount,0); } private Line credit(String code,String name,long amount) { return new Line(code,name,0,amount); }
    private String cashCode(ReceiptMethod method) { return method.name(); } private String cashName(ReceiptMethod method) { return method==ReceiptMethod.UPI?"UPI":method.name().substring(0,1)+method.name().substring(1).toLowerCase(); }
    private record Line(String code,String name,long debit,long credit) { } private record Header(String id,LocalDate date,String type,String narration) { }
}

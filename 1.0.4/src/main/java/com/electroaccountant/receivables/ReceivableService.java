package com.electroaccountant.receivables;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.electroaccountant.party.PartyRepository;
import com.electroaccountant.party.PartyService;
import com.electroaccountant.shared.ApiException;
import com.electroaccountant.shared.Money;
import com.electroaccountant.journal.JournalService;

@Service
public class ReceivableService {
    private final ReceivableRepository repository; private final PartyService partyService; private final JournalService journals;
    public ReceivableService(ReceivableRepository repository, PartyService partyService, JournalService journals) { this.repository = repository; this.partyService = partyService; this.journals = journals; }
    public CustomerBalanceResponse balance(String customerId) { PartyRepository.PartyRow customer = partyService.requiredCustomer(customerId); long sales = repository.creditSales(customerId), receipts = repository.receipts(customerId); return new CustomerBalanceResponse(customerId, customer.name(), Money.fromMinor(sales), Money.fromMinor(receipts), Money.fromMinor(sales - receipts)); }
    public List<CustomerReceiptResponse> receipts() { return repository.findAll().stream().map(this::response).toList(); }
    public List<CustomerReceiptResponse> receipts(String customerId) { return repository.findByCustomer(customerId).stream().map(this::response).toList(); }
    @Transactional public CustomerReceiptResponse create(CreateCustomerReceiptRequest request) { PartyRepository.PartyRow customer = partyService.requiredCustomer(request.customerId()); long amount = Money.toMinor(request.amount()); CustomerBalanceResponse balance = balance(request.customerId()); if (amount > Money.toMinor(balance.outstanding())) throw new ApiException(HttpStatus.BAD_REQUEST, "Receipt cannot exceed outstanding balance of " + balance.outstanding()); String id = UUID.randomUUID().toString(); repository.insert(id, customer.id(), request.receiptDate(), amount, request.method(), blank(request.referenceNumber()), blank(request.notes())); journals.postCustomerReceipt(id, request.receiptDate(), customer.name(), amount, request.method()); return new CustomerReceiptResponse(id, customer.id(), customer.name(), request.receiptDate(), Money.fromMinor(amount), request.method(), blank(request.referenceNumber()), blank(request.notes())); }
    private CustomerReceiptResponse response(ReceivableRepository.ReceiptRow row) { return new CustomerReceiptResponse(row.id(), row.customerId(), row.customerName(), row.receiptDate(), Money.fromMinor(row.amountMinor()), row.method(), row.referenceNumber(), row.notes()); }
    private String blank(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}

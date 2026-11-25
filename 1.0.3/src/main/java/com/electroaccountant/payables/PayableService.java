package com.electroaccountant.payables;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.electroaccountant.journal.JournalService;
import com.electroaccountant.party.PartyRepository;
import com.electroaccountant.party.PartyService;
import com.electroaccountant.shared.ApiException;
import com.electroaccountant.shared.Money;

@Service
public class PayableService {
    private final PayableRepository repository; private final PartyService partyService; private final JournalService journals;
    public PayableService(PayableRepository repository, PartyService partyService, JournalService journals) { this.repository=repository; this.partyService=partyService; this.journals=journals; }
    public SupplierBalanceResponse balance(String supplierId) { PartyRepository.PartyRow p=partyService.requiredSupplier(supplierId); long purchases=repository.creditPurchases(supplierId), expenses=repository.creditExpenses(supplierId), payments=repository.payments(supplierId); return new SupplierBalanceResponse(p.id(),p.name(),Money.fromMinor(purchases),Money.fromMinor(expenses),Money.fromMinor(purchases+expenses-payments)); }
    public List<SupplierPaymentResponse> payments() { return repository.findAll().stream().map(r->new SupplierPaymentResponse(r.id(),r.supplierId(),r.supplierName(),r.date(),Money.fromMinor(r.amountMinor()),r.method(),r.reference(),r.notes())).toList(); }
    @Transactional public SupplierPaymentResponse create(CreateSupplierPaymentRequest request) { PartyRepository.PartyRow supplier=partyService.requiredSupplier(request.supplierId()); long amount=Money.toMinor(request.amount()); SupplierBalanceResponse balance=balance(supplier.id()); if(amount>Money.toMinor(balance.outstanding())) throw new ApiException(HttpStatus.BAD_REQUEST,"Payment cannot exceed outstanding balance of "+balance.outstanding()); String id=UUID.randomUUID().toString(); repository.insert(id,supplier.id(),request.paymentDate(),amount,request.method(),clean(request.referenceNumber()),clean(request.notes())); journals.postSupplierPayment(id,request.paymentDate(),supplier.name(),amount,request.method()); return new SupplierPaymentResponse(id,supplier.id(),supplier.name(),request.paymentDate(),Money.fromMinor(amount),request.method(),clean(request.referenceNumber()),clean(request.notes())); }
    private String clean(String v) { return v==null||v.isBlank()?null:v.trim(); }
}

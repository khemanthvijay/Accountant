package com.electroaccountant.trade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.electroaccountant.inventory.InventoryRepository;
import com.electroaccountant.inventory.InventoryService;
import com.electroaccountant.party.PartyRepository;
import com.electroaccountant.party.PartyService;
import com.electroaccountant.shared.ApiException;
import com.electroaccountant.shared.Money;
import com.electroaccountant.shared.Quantity;
import com.electroaccountant.journal.InventoryCostService;
import com.electroaccountant.journal.JournalService;

@Service
public class TradeEntryService {
    private final InventoryService inventoryService;
    private final TradeEntryRepository tradeEntryRepository;
    private final PartyService partyService;
    private final JournalService journals;
    private final InventoryCostService inventoryCosts;

    public TradeEntryService(InventoryService inventoryService, TradeEntryRepository tradeEntryRepository, PartyService partyService, JournalService journals, InventoryCostService inventoryCosts) {
        this.inventoryService = inventoryService;
        this.tradeEntryRepository = tradeEntryRepository;
        this.partyService = partyService;
        this.journals = journals;
        this.inventoryCosts = inventoryCosts;
    }

    @Transactional
    public TradeEntryResponse createPurchase(CreatePurchaseRequest request) {
        PartyRepository.PartyRow supplier = partyService.requiredSupplier(request.supplierId());
        return create("PURCHASE", request.entryDate(), supplier.id(), supplier.name(), request.paymentType().name(), request.supplierInvoiceNumber(), request.notes(), request.lines());
    }

    @Transactional
    public TradeEntryResponse updatePurchase(String id, CreatePurchaseRequest request) {
        PartyRepository.PartyRow supplier = partyService.requiredSupplier(request.supplierId());
        return update("PURCHASE", id, request.entryDate(), supplier.id(), supplier.name(), request.paymentType().name(), request.supplierInvoiceNumber(), request.notes(), request.lines());
    }

    @Transactional
    public TradeEntryResponse createSale(CreateSaleRequest request) {
        PartyRepository.PartyRow customer = partyService.requiredCustomer(request.customerId());
        return create("SALE", request.entryDate(), customer.id(), customer.name(), request.paymentType().name(), request.invoiceNumber(), request.notes(), request.lines());
    }

    @Transactional
    public TradeEntryResponse updateSale(String id, CreateSaleRequest request) {
        PartyRepository.PartyRow customer = partyService.requiredCustomer(request.customerId());
        return update("SALE", id, request.entryDate(), customer.id(), customer.name(), request.paymentType().name(), request.invoiceNumber(), request.notes(), request.lines());
    }

    private TradeEntryResponse create(String type, LocalDate entryDate, String partyId, String partyName, String paymentType, String documentNumber, String notes, List<EntryLineRequest> requests) {
        List<CalculatedLine> lines = calculateLines(requests);
        if (type.equals("SALE")) validateStock(lines);

        String entryId = UUID.randomUUID().toString();
        long total = lines.stream().mapToLong(CalculatedLine::lineTotalMinor).sum();
        long saleCost = type.equals("SALE") ? inventoryCosts.costOfSale(entryDate, requests) : 0;
        tradeEntryRepository.insertHeader(type, entryId, partyId, entryDate, partyName.trim(), paymentType, blankToNull(documentNumber), blankToNull(notes), total);

        for (CalculatedLine line : lines) {
            tradeEntryRepository.insertLine(type, UUID.randomUUID().toString(), entryId, line.itemId(), line.itemName(),
                    line.quantityScaled(), line.unitAmountMinor(), line.lineTotalMinor());
            long stockDelta = type.equals("PURCHASE") ? line.quantityScaled() : -line.quantityScaled();
            inventoryService.recordMovement(line.itemId(), entryDate, type, stockDelta, type + "_ENTRY", entryId);
        }
        postJournal(type, entryId, entryDate, partyName, total, paymentType, saleCost);
        return response(entryId, type, entryDate, partyId, partyName, paymentType, documentNumber, notes, total, lines);
    }

    @Transactional
    private TradeEntryResponse update(String type, String entryId, LocalDate entryDate, String partyId, String partyName, String paymentType, String documentNumber, String notes, List<EntryLineRequest> requests) {
        if (tradeEntryRepository.findHeader(type, entryId).isEmpty()) throw new ApiException(HttpStatus.NOT_FOUND, "Entry not found");
        inventoryService.removeMovementsForSource(entryId);
        tradeEntryRepository.deleteLines(type, entryId);
        List<CalculatedLine> lines = calculateLines(requests);
        if (type.equals("SALE")) validateStock(lines);
        long total = lines.stream().mapToLong(CalculatedLine::lineTotalMinor).sum();
        long saleCost = type.equals("SALE") ? inventoryCosts.costOfSale(entryDate, requests) : 0;
        tradeEntryRepository.updateHeader(type, entryId, partyId, entryDate, partyName.trim(), paymentType, blankToNull(documentNumber), blankToNull(notes), total);
        for (CalculatedLine line : lines) {
            tradeEntryRepository.insertLine(type, UUID.randomUUID().toString(), entryId, line.itemId(), line.itemName(), line.quantityScaled(), line.unitAmountMinor(), line.lineTotalMinor());
            inventoryService.recordMovement(line.itemId(), entryDate, type, type.equals("PURCHASE") ? line.quantityScaled() : -line.quantityScaled(), type + "_ENTRY", entryId);
        }
        postJournal(type, entryId, entryDate, partyName, total, paymentType, saleCost);
        return response(entryId, type, entryDate, partyId, partyName, paymentType, documentNumber, notes, total, lines);
    }

    public List<TradeEntryResponse> dailyPurchases(LocalDate date) { return daily("PURCHASE", date); }
    public List<TradeEntryResponse> dailySales(LocalDate date) { return daily("SALE", date); }
    public TradeEntryResponse purchase(String id) { return entry("PURCHASE", id); }
    public TradeEntryResponse sale(String id) { return entry("SALE", id); }

    private List<TradeEntryResponse> daily(String type, LocalDate date) {
        return tradeEntryRepository.findHeaders(type, date).stream().map(header -> {
            List<CalculatedLine> lines = tradeEntryRepository.findLines(type, header.id()).stream()
                    .map(line -> new CalculatedLine(line.itemId(), line.itemName(), line.quantityScaled(), line.unitAmountMinor(), line.lineTotalMinor()))
                    .toList();
            return response(header.id(), type, header.date(), header.partyId(), header.partyName(), header.paymentType(), header.documentNumber(), header.notes(), header.totalMinor(), lines);
        }).toList();
    }

    private TradeEntryResponse entry(String type, String id) {
        TradeEntryRepository.EntryRow header = tradeEntryRepository.findHeader(type, id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Entry not found"));
        List<CalculatedLine> lines = tradeEntryRepository.findLines(type, id).stream().map(line -> new CalculatedLine(line.itemId(), line.itemName(), line.quantityScaled(), line.unitAmountMinor(), line.lineTotalMinor())).toList();
        return response(header.id(), type, header.date(), header.partyId(), header.partyName(), header.paymentType(), header.documentNumber(), header.notes(), header.totalMinor(), lines);
    }

    private List<CalculatedLine> calculateLines(List<EntryLineRequest> requests) {
        List<CalculatedLine> lines = new ArrayList<>();
        for (EntryLineRequest request : requests) {
            InventoryRepository.ItemRow item = inventoryService.requiredActiveItem(request.itemId());
            long quantity = Quantity.toPositiveScaled(request.quantity());
            long unitAmount = Money.toMinor(request.unitAmount());
            lines.add(new CalculatedLine(item.id(), item.name(), quantity, unitAmount, Money.lineTotal(quantity, unitAmount)));
        }
        return lines;
    }

    private void validateStock(List<CalculatedLine> lines) {
        Map<String, Long> requestedByItem = new LinkedHashMap<>();
        for (CalculatedLine line : lines) {
            requestedByItem.merge(line.itemId(), line.quantityScaled(), Math::addExact);
        }
        for (Map.Entry<String, Long> requested : requestedByItem.entrySet()) {
            long available = inventoryService.availableQuantity(requested.getKey());
            if (requested.getValue() > available) {
                throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock for item " + requested.getKey()
                        + ". Available: " + Quantity.fromScaled(available) + ", requested: " + Quantity.fromScaled(requested.getValue()));
            }
        }
    }

    private TradeEntryResponse response(String id, String type, LocalDate date, String partyId, String party, String paymentType, String documentNumber, String notes, long total, List<CalculatedLine> lines) {
        List<EntryLineResponse> responseLines = lines.stream().map(line -> new EntryLineResponse(line.itemId(), line.itemName(),
                Quantity.fromScaled(line.quantityScaled()), Money.fromMinor(line.unitAmountMinor()), Money.fromMinor(line.lineTotalMinor()))).toList();
        return new TradeEntryResponse(id, type, date, partyId, party, paymentType, documentNumber, notes, Money.fromMinor(total), responseLines);
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private void postJournal(String type, String id, LocalDate date, String partyName, long total, String paymentType, long saleCost) { if (type.equals("PURCHASE")) journals.postPurchase(id,date,partyName,total,PaymentType.valueOf(paymentType)); else journals.postSale(id,date,partyName,total,PaymentType.valueOf(paymentType),saleCost); }
    private record CalculatedLine(String itemId, String itemName, long quantityScaled, long unitAmountMinor, long lineTotalMinor) { }
}

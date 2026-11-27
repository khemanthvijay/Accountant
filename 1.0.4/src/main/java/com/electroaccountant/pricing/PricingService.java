package com.electroaccountant.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.electroaccountant.shared.ApiException;
import com.electroaccountant.shared.Money;
import com.electroaccountant.shared.Quantity;
import com.electroaccountant.trade.PaymentType;

@Service
public class PricingService {
    private static final BigDecimal BASIS_POINTS = BigDecimal.valueOf(10_000);
    private final JdbcTemplate jdbc;
    public PricingService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<PricingGroupResponse> groups() {
        return jdbc.query("SELECT id, name FROM pricing_group WHERE active = 1 ORDER BY name", (rs, row) -> response(rs.getString("id"), rs.getString("name")));
    }

    public PricingGroupResponse group(String id) {
        return jdbc.query("SELECT id, name FROM pricing_group WHERE id = ? AND active = 1", (rs, row) -> response(rs.getString("id"), rs.getString("name")), id)
                .stream().findFirst().orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pricing group not found"));
    }

    @Transactional
    public PricingGroupResponse create(PricingGroupRequest request) {
        String id = UUID.randomUUID().toString();
        save(id, request, true);
        return group(id);
    }

    @Transactional
    public PricingGroupResponse update(String id, PricingGroupRequest request) {
        group(id);
        save(id, request, false);
        return group(id);
    }

    public PricingQuoteResponse quote(PricingQuoteRequest request) {
        long quantity = Quantity.toPositiveScaled(request.quantity());
        ItemPrice item = jdbc.query("SELECT id, default_sales_rate_minor FROM inventory_item WHERE id = ? AND active = 1",
                (rs, row) -> new ItemPrice(rs.getString("id"), rs.getLong("default_sales_rate_minor")), request.itemId())
                .stream().findFirst().orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Active item not found"));
        GroupRef group = jdbc.query("SELECT g.id, g.name FROM pricing_group_item gi JOIN pricing_group g ON g.id = gi.pricing_group_id WHERE gi.item_id = ? AND g.active = 1",
                (rs, row) -> new GroupRef(rs.getString("id"), rs.getString("name")), request.itemId()).stream().findFirst().orElse(null);
        int paymentAdjustment = group == null ? 0 : adjustment(group.id(), request.paymentType().name());
        int quantityAdjustment = group == null ? 0 : quantityAdjustment(group.id(), quantity);
        long afterPayment = apply(item.mrpMinor(), paymentAdjustment);
        long calculated = apply(afterPayment, quantityAdjustment);
        return new PricingQuoteResponse(item.id(), group == null ? null : group.name(), Money.fromMinor(item.mrpMinor()), percent(paymentAdjustment), percent(quantityAdjustment), Money.fromMinor(calculated));
    }

    private void save(String id, PricingGroupRequest request, boolean creating) {
        String name = request.name().trim();
        validate(request);
        try {
            if (creating) jdbc.update("INSERT INTO pricing_group (id, name, created_at) VALUES (?, ?, ?)", id, name, Instant.now().toString());
            else jdbc.update("UPDATE pricing_group SET name = ? WHERE id = ?", name, id);
            jdbc.update("DELETE FROM pricing_group_item WHERE pricing_group_id = ?", id);
            jdbc.update("DELETE FROM pricing_payment_adjustment WHERE pricing_group_id = ?", id);
            jdbc.update("DELETE FROM pricing_quantity_adjustment WHERE pricing_group_id = ?", id);
            for (String itemId : request.itemIds().stream().distinct().toList()) jdbc.update("INSERT INTO pricing_group_item (pricing_group_id, item_id) VALUES (?, ?)", id, itemId);
            for (PaymentType type : List.of(PaymentType.CASH, PaymentType.BANK, PaymentType.CREDIT)) {
                BigDecimal adjustment = request.paymentAdjustments().getOrDefault(type, BigDecimal.ZERO);
                jdbc.update("INSERT INTO pricing_payment_adjustment (pricing_group_id, payment_type, adjustment_basis_points) VALUES (?, ?, ?)", id, type.name(), basisPoints(adjustment));
            }
            for (PricingQuantityRuleRequest rule : request.quantityRules()) jdbc.update("INSERT INTO pricing_quantity_adjustment (id, pricing_group_id, minimum_quantity_scaled, maximum_quantity_scaled, adjustment_basis_points) VALUES (?, ?, ?, ?, ?)", UUID.randomUUID().toString(), id, Quantity.toPositiveScaled(rule.minimumQuantity()), rule.maximumQuantity() == null ? null : Quantity.toPositiveScaled(rule.maximumQuantity()), basisPoints(rule.adjustmentPercent()));
        } catch (org.springframework.dao.DataAccessException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "An item can belong to only one pricing group, and group names must be unique");
        }
    }

    private PricingGroupResponse response(String id, String name) {
        List<String> itemIds = jdbc.queryForList("SELECT item_id FROM pricing_group_item WHERE pricing_group_id = ? ORDER BY item_id", String.class, id);
        Map<PaymentType, BigDecimal> payment = jdbc.query("SELECT payment_type, adjustment_basis_points FROM pricing_payment_adjustment WHERE pricing_group_id = ?", (rs, row) -> Map.entry(PaymentType.valueOf(rs.getString(1)), percent(rs.getInt(2))), id).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<PricingQuantityRuleRequest> rules = jdbc.query("SELECT minimum_quantity_scaled, maximum_quantity_scaled, adjustment_basis_points FROM pricing_quantity_adjustment WHERE pricing_group_id = ? ORDER BY minimum_quantity_scaled", (rs, row) -> new PricingQuantityRuleRequest(Quantity.fromScaled(rs.getLong(1)), rs.getObject(2) == null ? null : Quantity.fromScaled(rs.getLong(2)), percent(rs.getInt(3))), id);
        return new PricingGroupResponse(id, name, itemIds, payment, rules);
    }
    private int adjustment(String groupId, String type) { Integer value = jdbc.queryForObject("SELECT adjustment_basis_points FROM pricing_payment_adjustment WHERE pricing_group_id = ? AND payment_type = ?", Integer.class, groupId, type); return value == null ? 0 : value; }
    private int quantityAdjustment(String groupId, long quantity) { return jdbc.query("SELECT adjustment_basis_points FROM pricing_quantity_adjustment WHERE pricing_group_id = ? AND minimum_quantity_scaled <= ? AND (maximum_quantity_scaled IS NULL OR maximum_quantity_scaled >= ?) ORDER BY minimum_quantity_scaled DESC LIMIT 1", (rs, row) -> rs.getInt(1), groupId, quantity, quantity).stream().findFirst().orElse(0); }
    private long apply(long amount, int basisPoints) { return BigDecimal.valueOf(amount).multiply(BASIS_POINTS.add(BigDecimal.valueOf(basisPoints))).divide(BASIS_POINTS, 0, RoundingMode.HALF_UP).longValueExact(); }
    private BigDecimal percent(int basisPoints) { return BigDecimal.valueOf(basisPoints, 2); }
    private int basisPoints(BigDecimal percentage) { try { return percentage.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).intValueExact(); } catch (ArithmeticException ex) { throw new ApiException(HttpStatus.BAD_REQUEST, "Price adjustment supports at most two decimal places"); } }
    private void validate(PricingGroupRequest request) { if (request.itemIds().isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Select at least one item"); for (PricingQuantityRuleRequest rule : request.quantityRules()) { if (rule.minimumQuantity().signum() <= 0 || (rule.maximumQuantity() != null && rule.maximumQuantity().compareTo(rule.minimumQuantity()) < 0)) throw new ApiException(HttpStatus.BAD_REQUEST, "Each quantity range must have a valid minimum and maximum"); } }
    private record ItemPrice(String id, long mrpMinor) { }
    private record GroupRef(String id, String name) { }
}

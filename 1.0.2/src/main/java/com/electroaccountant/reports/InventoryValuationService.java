package com.electroaccountant.reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.electroaccountant.shared.Money;

@Service
public class InventoryValuationService {
    private final JdbcTemplate jdbc;
    public InventoryValuationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public TradingReportResponse trading(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) throw new IllegalArgumentException("To date must be on or after from date");
        Map<String, ValueState> states = new HashMap<>();
        long opening = 0, purchases = 0, sales = 0, cogs = 0;
        for (ValuationEvent event : events()) {
            if (event.date().isBefore(from)) { apply(states, event); continue; }
            if (event.date().isAfter(to)) break;
            if (event.kind().equals("SALE")) {
                long cost = applySale(states, event);
                sales = Math.addExact(sales, event.revenueMinor());
                cogs = Math.addExact(cogs, cost);
            } else {
                apply(states, event);
                if (event.kind().equals("PURCHASE")) purchases = Math.addExact(purchases, event.valueMinor());
            }
            if (event.date().equals(from) && event.kind().equals("OPENING")) opening = Math.addExact(opening, event.valueMinor());
        }
        // Opening is the value held immediately before the report period, plus any explicit opening entry on its first day.
        long beforePeriod = openingValueBefore(from);
        long closing = states.values().stream().mapToLong(ValueState::valueMinor).sum();
        return new TradingReportResponse(from, to, Money.fromMinor(beforePeriod + opening), Money.fromMinor(purchases), Money.fromMinor(sales),
                Money.fromMinor(closing), Money.fromMinor(cogs), Money.fromMinor(sales - cogs));
    }

    private long openingValueBefore(LocalDate from) {
        Map<String, ValueState> states = new HashMap<>();
        for (ValuationEvent event : events()) {
            if (!event.date().isBefore(from)) break;
            apply(states, event);
        }
        return states.values().stream().mapToLong(ValueState::valueMinor).sum();
    }

    private void apply(Map<String, ValueState> states, ValuationEvent event) {
        ValueState state = states.computeIfAbsent(event.itemId(), ignored -> new ValueState());
        if (event.kind().equals("SALE")) { applySale(states, event); return; }
        state.quantityScaled = Math.addExact(state.quantityScaled, event.quantityScaled());
        state.valueMinor = Math.addExact(state.valueMinor, event.valueMinor());
    }

    private long applySale(Map<String, ValueState> states, ValuationEvent event) {
        ValueState state = states.computeIfAbsent(event.itemId(), ignored -> new ValueState());
        long quantity = Math.abs(event.quantityScaled());
        long cost = state.quantityScaled <= 0 ? 0 : BigDecimal.valueOf(state.valueMinor)
                .multiply(BigDecimal.valueOf(quantity)).divide(BigDecimal.valueOf(state.quantityScaled), 0, RoundingMode.HALF_UP).longValueExact();
        state.quantityScaled -= quantity;
        state.valueMinor -= cost;
        return cost;
    }

    private List<ValuationEvent> events() {
        return jdbc.query("""
                SELECT item_id, event_date, event_time, event_kind, quantity_scaled, value_minor, revenue_minor
                FROM (
                    SELECT sm.item_id, sm.movement_date AS event_date, sm.created_at AS event_time, 'OPENING' AS event_kind,
                           sm.quantity_delta_scaled AS quantity_scaled, sm.quantity_delta_scaled * i.opening_rate_minor AS value_minor, 0 AS revenue_minor
                    FROM stock_movement sm JOIN inventory_item i ON i.id = sm.item_id WHERE sm.movement_type = 'OPENING'
                    UNION ALL
                    SELECT pl.item_id, pe.entry_date, pe.created_at, 'PURCHASE', pl.quantity_scaled, pl.line_total_minor, 0
                    FROM purchase_entry_line pl JOIN purchase_entry pe ON pe.id = pl.purchase_entry_id
                    UNION ALL
                    SELECT sl.item_id, se.entry_date, se.created_at, 'SALE', -sl.quantity_scaled, 0, sl.line_total_minor
                    FROM sales_entry_line sl JOIN sales_entry se ON se.id = sl.sales_entry_id
                ) ORDER BY event_date, event_time,
                    CASE event_kind WHEN 'OPENING' THEN 0 WHEN 'PURCHASE' THEN 1 ELSE 2 END
                """, (rs, row) -> new ValuationEvent(rs.getString("item_id"), LocalDate.parse(rs.getString("event_date")),
                rs.getString("event_kind"), rs.getLong("quantity_scaled"), rs.getLong("value_minor"), rs.getLong("revenue_minor")));
    }

    private record ValuationEvent(String itemId, LocalDate date, String kind, long quantityScaled, long valueMinor, long revenueMinor) { }
    private static final class ValueState { long quantityScaled; long valueMinor; long valueMinor() { return valueMinor; } }
}

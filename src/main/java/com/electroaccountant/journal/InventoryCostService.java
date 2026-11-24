package com.electroaccountant.journal;

import java.math.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.electroaccountant.trade.EntryLineRequest;
import com.electroaccountant.shared.Quantity;

/** Calculates weighted-average cost at the moment a sale is posted. */
@Service public class InventoryCostService {
    private final JdbcTemplate jdbc; public InventoryCostService(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public long costOfSale(LocalDate date,List<EntryLineRequest> lines){Map<String,State> states=new HashMap<>(); for(Event e:eventsUntil(date))apply(states,e); long total=0;for(EntryLineRequest line:lines){State s=states.computeIfAbsent(line.itemId(),ignored->new State());long qty=Quantity.toPositiveScaled(line.quantity());long cost=s.qty==0?0:BigDecimal.valueOf(s.value).multiply(BigDecimal.valueOf(qty)).divide(BigDecimal.valueOf(s.qty),0,RoundingMode.HALF_UP).longValue();s.qty-=qty;s.value-=cost;total=Math.addExact(total,cost);}return total;}
    private void apply(Map<String,State> states,Event e){State s=states.computeIfAbsent(e.item,ignored->new State());if(e.sale){long qty=-e.qty;long cost=s.qty==0?0:BigDecimal.valueOf(s.value).multiply(BigDecimal.valueOf(qty)).divide(BigDecimal.valueOf(s.qty),0,RoundingMode.HALF_UP).longValue();s.qty-=qty;s.value-=cost;}else{s.qty+=e.qty;s.value+=e.value;}}
    private List<Event> eventsUntil(LocalDate to){return jdbc.query("""
        SELECT item_id,event_date,event_time,event_kind,quantity_scaled,value_minor FROM (
          SELECT sm.item_id,sm.movement_date event_date,sm.created_at event_time,'OPENING' event_kind,sm.quantity_delta_scaled,i.opening_rate_minor*sm.quantity_delta_scaled value_minor FROM stock_movement sm JOIN inventory_item i ON i.id=sm.item_id WHERE sm.movement_type='OPENING'
          UNION ALL SELECT pl.item_id,pe.entry_date,pe.created_at,'PURCHASE',pl.quantity_scaled,pl.line_total_minor FROM purchase_entry_line pl JOIN purchase_entry pe ON pe.id=pl.purchase_entry_id
          UNION ALL SELECT sl.item_id,se.entry_date,se.created_at,'SALE',-sl.quantity_scaled,0 FROM sales_entry_line sl JOIN sales_entry se ON se.id=sl.sales_entry_id
        ) WHERE event_date<=? ORDER BY event_date,event_time,CASE event_kind WHEN 'OPENING' THEN 0 WHEN 'PURCHASE' THEN 1 ELSE 2 END
        """,(rs,n)->new Event(rs.getString("item_id"),rs.getLong("quantity_scaled"),rs.getLong("value_minor"),"SALE".equals(rs.getString("event_kind"))),to.toString());}
    private static final class State{long qty;long value;} private record Event(String item,long qty,long value,boolean sale){}
}

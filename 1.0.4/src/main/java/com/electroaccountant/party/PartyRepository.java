package com.electroaccountant.party;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PartyRepository {
    private final JdbcTemplate jdbc;
    public PartyRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void insert(String id, LedgerType ledgerType, String name, String phone, String email, String address) {
        jdbc.update("INSERT INTO party (id, party_type, ledger_type, name, phone, email, address, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, ledgerType.legacyPartyType(), ledgerType.name(), name, phone, email, address, Instant.now().toString());
    }

    public List<PartyRow> findAllActive() {
        return jdbc.query("SELECT id, name, ledger_type, phone, email, address FROM party WHERE active = 1 ORDER BY name", (rs, row) -> row(rs));
    }

    public Optional<PartyRow> findActive(String id) {
        return jdbc.query("SELECT id, name, ledger_type, phone, email, address FROM party WHERE id = ? AND active = 1", (rs, row) -> row(rs), id)
                .stream().findFirst();
    }

    public void update(String id, LedgerType ledgerType, String name, String phone, String email, String address) {
        jdbc.update("UPDATE party SET party_type = ?, ledger_type = ?, name = ?, phone = ?, email = ?, address = ? WHERE id = ? AND active = 1",
                ledgerType.legacyPartyType(), ledgerType.name(), name, phone, email, address, id);
    }

    private PartyRow row(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new PartyRow(rs.getString("id"), rs.getString("name"), LedgerType.valueOf(rs.getString("ledger_type")), rs.getString("phone"), rs.getString("email"), rs.getString("address"));
    }
    public record PartyRow(String id, String name, LedgerType ledgerType, String phone, String email, String address) { }
}

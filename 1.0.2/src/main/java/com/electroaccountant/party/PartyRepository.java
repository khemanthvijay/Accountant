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

    public void insert(String id, String type, String name, String phone, String email, String address) {
        jdbc.update("INSERT INTO party (id, party_type, name, phone, email, address, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, type, name, phone, email, address, Instant.now().toString());
    }

    public List<PartyRow> findActiveByType(String type) {
        return jdbc.query("SELECT id, name, phone, email, address FROM party WHERE party_type = ? AND active = 1 ORDER BY name", (rs, row) -> row(rs), type);
    }

    public Optional<PartyRow> findActive(String id, String type) {
        return jdbc.query("SELECT id, name, phone, email, address FROM party WHERE id = ? AND party_type = ? AND active = 1", (rs, row) -> row(rs), id, type)
                .stream().findFirst();
    }

    public void update(String id, String type, String name, String phone, String email, String address) {
        jdbc.update("UPDATE party SET name = ?, phone = ?, email = ?, address = ? WHERE id = ? AND party_type = ? AND active = 1",
                name, phone, email, address, id, type);
    }

    private PartyRow row(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new PartyRow(rs.getString("id"), rs.getString("name"), rs.getString("phone"), rs.getString("email"), rs.getString("address"));
    }
    public record PartyRow(String id, String name, String phone, String email, String address) { }
}

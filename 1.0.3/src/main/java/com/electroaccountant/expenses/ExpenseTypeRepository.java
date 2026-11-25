package com.electroaccountant.expenses;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository public class ExpenseTypeRepository { private final JdbcTemplate jdbc; public ExpenseTypeRepository(JdbcTemplate jdbc){this.jdbc=jdbc;} public List<Row> all(){return jdbc.query("SELECT id,name,account_code FROM expense_type_master WHERE active=1 ORDER BY name",(rs,n)->new Row(rs.getString("id"),rs.getString("name"),rs.getString("account_code")));} public Optional<Row> find(String id){return jdbc.query("SELECT id,name,account_code FROM expense_type_master WHERE id=? AND active=1",(rs,n)->new Row(rs.getString("id"),rs.getString("name"),rs.getString("account_code")),id).stream().findFirst();} public void insert(String id,String name,String code){jdbc.update("INSERT INTO expense_type_master (id,name,account_code,created_at) VALUES (?,?,?,?)",id,name,code,Instant.now().toString());} public record Row(String id,String name,String accountCode){} }

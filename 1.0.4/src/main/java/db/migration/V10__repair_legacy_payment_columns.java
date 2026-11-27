package db.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Handles old SQLite files whose Flyway history contains V3/V5 but lacks their columns. */
public class V10__repair_legacy_payment_columns extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        addColumnIfMissing(connection, "sales_entry", "payment_type", "TEXT NOT NULL DEFAULT 'CREDIT'");
        addColumnIfMissing(connection, "purchase_entry", "payment_type", "TEXT NOT NULL DEFAULT 'CREDIT'");
        addColumnIfMissing(connection, "sales_entry", "payment_type_v2", "TEXT NOT NULL DEFAULT 'CREDIT'");
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE sales_entry SET payment_type_v2 = payment_type WHERE payment_type_v2 IS NULL OR payment_type_v2 = 'CASH'");
        }
    }

    private void addColumnIfMissing(Connection connection, String table, String column, String definition) throws Exception {
        if (hasColumn(connection, table, column)) return;
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private boolean hasColumn(Connection connection, String table, String column) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) if (column.equalsIgnoreCase(columns.getString("name"))) return true;
            return false;
        }
    }
}

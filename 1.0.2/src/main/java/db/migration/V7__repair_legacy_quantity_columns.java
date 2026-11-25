package db.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Repairs databases created by the pre-1.0.2 schema, which stored quantities in
 * unscaled columns. The original V1 migration cannot be changed for databases
 * that already have Flyway history, so this migration is deliberately
 * conditional and is also safe for a newly created database.
 */
public class V7__repair_legacy_quantity_columns extends BaseJavaMigration {
    private static final long QUANTITY_SCALE = 1_000L;

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        ensureScaledQuantity(connection, "purchase_entry_line", "quantity_scaled", "quantity");
        ensureScaledQuantity(connection, "sales_entry_line", "quantity_scaled", "quantity");
        ensureScaledQuantity(connection, "stock_movement", "quantity_delta_scaled", "quantity_delta");
        ensureScaledQuantity(connection, "inventory_item", "opening_quantity_scaled", "opening_quantity");
        ensureScaledQuantity(connection, "inventory_item", "reorder_level_scaled", "reorder_level");
    }

    private void ensureScaledQuantity(Connection connection, String table, String scaledColumn, String legacyColumn)
            throws SQLException {
        if (hasColumn(connection, table, scaledColumn)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + scaledColumn + " INTEGER NOT NULL DEFAULT 0");
            if (hasColumn(connection, table, legacyColumn)) {
                statement.executeUpdate("UPDATE " + table + " SET " + scaledColumn
                        + " = CAST(ROUND(CAST(" + legacyColumn + " AS REAL) * " + QUANTITY_SCALE + ") AS INTEGER)");
            }
        }
    }

    private boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (columns.next()) {
                if (column.equalsIgnoreCase(columns.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}

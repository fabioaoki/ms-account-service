package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Renomeia a tabela da V17 para nome neutro. Em bases novas que já não têm o nome antigo, não faz nada.
 */
public class V18__rename_bishop_session_to_account_text_ai extends BaseJavaMigration {

    private static final String OLD_TABLE = "bishop_ministry_assistant_session";
    private static final String NEW_TABLE = "account_text_ai_session";

    @Override
    public void migrate(Context context) throws Exception {
        if (!tableExists(context, OLD_TABLE)) {
            return;
        }
        try (Statement st = context.getConnection().createStatement()) {
            st.execute("ALTER TABLE " + OLD_TABLE + " RENAME TO " + NEW_TABLE);
        }
        try (Statement st = context.getConnection().createStatement()) {
            st.execute(
                    "ALTER TABLE " + NEW_TABLE + " RENAME CONSTRAINT fk_bishop_ministry_session_account TO "
                            + "fk_account_text_ai_session_account"
            );
        } catch (Exception ignored) {
            // Nome já alinhado ou dialecto sem rename de constraint.
        }
        renameIndexIfExists(context, NEW_TABLE, "uk_bishop_ministry_openai_thread", "uk_account_text_ai_openai_thread");
        renameIndexIfExists(context, NEW_TABLE, "idx_bishop_ministry_session_account", "idx_account_text_ai_session_account");
    }

    private static boolean tableExists(Context context, String table) throws Exception {
        DatabaseMetaData meta = context.getConnection().getMetaData();
        try (ResultSet rs = meta.getTables(null, null, table, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static void renameIndexIfExists(Context context, String table, String oldName, String newName)
            throws Exception {
        if (!indexExistsOnTable(context, table, oldName)) {
            return;
        }
        try (Statement st = context.getConnection().createStatement()) {
            st.execute("ALTER INDEX " + oldName + " RENAME TO " + newName);
        }
    }

    private static boolean indexExistsOnTable(Context context, String table, String indexName) throws Exception {
        DatabaseMetaData meta = context.getConnection().getMetaData();
        try (ResultSet rs = meta.getIndexInfo(null, null, table, false, false)) {
            while (rs.next()) {
                if (indexName.equalsIgnoreCase(rs.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }
}

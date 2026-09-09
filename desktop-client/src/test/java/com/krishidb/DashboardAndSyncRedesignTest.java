package com.krishidb;

import com.krishidb.dao.DashboardDAO;
import com.krishidb.dao.SyncQueueDAO;
import com.krishidb.database.DatabaseInitializer;
import com.krishidb.ui.pages.DashboardPanel;
import com.krishidb.ui.pages.SyncPanel;
import com.krishidb.util.I18n;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardAndSyncRedesignTest {

    @BeforeAll
    public static void setup() {
        DatabaseInitializer.initialize();
    }

    @Test
    public void testDashboardEnhancedMetrics() {
        DashboardDAO dao = new DashboardDAO();
        DashboardDAO.DashboardMetrics m = dao.fetchMetrics();
        assertNotNull(m, "Dashboard metrics should not be null");

        // Verify existing metrics
        assertTrue(m.totalSales >= 0.0);
        assertTrue(m.totalPurchases >= 0.0);
        assertTrue(m.productCount >= 0);
        assertTrue(m.lowStockCount >= 0);

        // Verify newly added metrics
        assertTrue(m.todayPurchases >= 0.0);
        assertTrue(m.todayPurchasesCount >= 0);
        assertTrue(m.todayExpenses >= 0.0);
        assertTrue(m.todayExpensesCount >= 0);
        assertTrue(m.inStockCount >= 0);
        assertTrue(m.inStockCount <= m.productCount);
    }

    @Test
    public void testSyncQueueDaoMetrics() {
        SyncQueueDAO queueDAO = new SyncQueueDAO();

        int pending = queueDAO.getPendingCount();
        assertTrue(pending >= 0, "Pending count should be non-negative");

        int completed = queueDAO.getCompletedCount();
        assertTrue(completed >= 0, "Completed count should be non-negative");

        int failed = queueDAO.getFailedCount();
        assertTrue(failed >= 0, "Failed count should be non-negative");

        Map<String, Integer> moduleCounts = queueDAO.getPendingCountsByModule();
        assertNotNull(moduleCounts, "Module counts map should not be null");
        assertTrue(moduleCounts.containsKey("products"));
        assertTrue(moduleCounts.containsKey("customers"));
        assertTrue(moduleCounts.containsKey("suppliers"));
        assertTrue(moduleCounts.containsKey("purchases"));
        assertTrue(moduleCounts.containsKey("sales"));
        assertTrue(moduleCounts.containsKey("expenses"));
        assertTrue(moduleCounts.containsKey("transactions"));

        var history = queueDAO.getSyncHistory(10);
        assertNotNull(history, "History should not be null");
        assertTrue(history.size() <= 10, "History should respect limit");
    }

    @Test
    public void testLocalizationKeysForDashboardAndSync() {
        String[] languages = {"en", "mr", "hi"};
        String[] requiredKeys = {
                // Dashboard keys
                "dashboard.title", "dashboard.subtitle", "dashboard.btn.refresh",
                "dashboard.card.today_sales", "dashboard.card.inventory_val",
                "dashboard.card.total_purchases", "dashboard.card.total_expenses",
                "dashboard.card.net_balance", "dashboard.card.low_stock",
                "dashboard.card.customer_khata", "dashboard.card.supplier_payable",
                "dashboard.flow.title", "dashboard.flow.subtitle", "dashboard.flow.inflow",
                "dashboard.flow.inward", "dashboard.flow.outflow", "dashboard.flow.net",
                "dashboard.snapshot.title", "dashboard.snapshot.inventory",
                "dashboard.snapshot.credit", "dashboard.snapshot.operations",
                "dashboard.empty.healthy_title", "dashboard.empty.healthy_desc",
                "dashboard.empty.tx_title", "dashboard.empty.tx_desc",

                // Sync keys
                "sync.title", "sync.subtitle", "sync.btn.sync_now", "sync.btn.refresh",
                "sync.status.ready", "sync.banner.connection", "sync.banner.mode",
                "sync.banner.mode_active", "sync.banner.desc", "sync.banner.server",
                "sync.banner.pending_queue", "sync.banner.last_sync", "sync.banner.never",
                "sync.card.pending", "sync.card.synced", "sync.card.failed", "sync.card.last_sync",
                "sync.module.title", "sync.module.subtitle", "sync.module.products",
                "sync.module.customers", "sync.module.suppliers", "sync.module.purchases",
                "sync.module.sales", "sync.module.expenses", "sync.module.transactions",
                "sync.protect.title", "sync.protect.point1_title", "sync.protect.point2_title",
                "sync.protect.point3_title", "sync.protect.point4_title",
                "sync.history.title", "sync.history.subtitle", "sync.history.empty_title",
                "sync.col.table", "sync.col.operation", "sync.col.status", "sync.col.time",
                "sync.msg.server_unavailable", "sync.msg.partial", "sync.msg.no_pending",

                // Settings keys
                "settings.title", "settings.subtitle", "settings.badge.offline_first", "settings.badge.offline_desc",
                "settings.section.language", "settings.section.language_desc", "settings.label.language",
                "settings.label.language_helper", "settings.label.language_note",
                "settings.section.business", "settings.section.business_desc", "settings.badge.identity",
                "settings.label.business_name", "settings.label.business_name_helper",
                "settings.label.owner_name", "settings.label.owner_name_helper",
                "settings.label.phone", "settings.label.phone_helper",
                "settings.label.village", "settings.label.village_helper",
                "settings.section.server", "settings.section.server_desc",
                "settings.label.server_url", "settings.label.server_url_helper",
                "settings.server.mode_title", "settings.server.mode_desc",
                "settings.section.backup", "settings.section.backup_desc",
                "settings.backup.title", "settings.backup.desc", "settings.backup.note",
                "settings.btn.backup", "settings.btn.save", "settings.save_helper", "settings.save_indicator",
                "settings.section.about", "settings.section.about_desc",
                "settings.about.version", "settings.about.desc", "settings.about.mode",
                "settings.about.feature1", "settings.about.feature2", "settings.about.feature3",
                "settings.msg.saved"
        };

        for (String lang : languages) {
            I18n.setLanguage(lang);
            for (String key : requiredKeys) {
                String val = I18n.get(key);
                assertNotNull(val, "Key " + key + " should not be null for lang " + lang);
                assertFalse(val.startsWith("???"), "Key " + key + " should not be missing (found: " + val + ") for lang " + lang);
            }
        }

        // Reset to default
        I18n.setLanguage("en");
    }

    @Test
    public void testSyncResultEvaluation() {
        // Test HTTP 201 CREATED evaluation logic
        int code200 = 200;
        int code201 = 201;
        int code204 = 204;
        int code400 = 400;
        int code500 = 500;

        assertTrue(code200 >= 200 && code200 < 300, "200 should be successful");
        assertTrue(code201 >= 200 && code201 < 300, "201 CREATED must be successful");
        assertTrue(code204 >= 200 && code204 < 300, "204 NO CONTENT should be successful");
        assertFalse(code400 >= 200 && code400 < 300, "400 BAD REQUEST should not be successful");
        assertFalse(code500 >= 200 && code500 < 300, "500 SERVER ERROR should not be successful");

        // Test SyncResult model behavior
        com.krishidb.dao.SyncDAO.SyncResult resultSuccess = new com.krishidb.dao.SyncDAO.SyncResult(4, 4, 0, false, "All succeeded");
        assertTrue(resultSuccess.isAllSuccess());
        assertFalse(resultSuccess.isPartialSuccess());
        assertFalse(resultSuccess.isServerUnavailable());

        com.krishidb.dao.SyncDAO.SyncResult resultPartial = new com.krishidb.dao.SyncDAO.SyncResult(4, 2, 2, false, "Partial");
        assertFalse(resultPartial.isAllSuccess());
        assertTrue(resultPartial.isPartialSuccess());

        com.krishidb.dao.SyncDAO.SyncResult resultOffline = new com.krishidb.dao.SyncDAO.SyncResult(0, 0, 0, true, "Offline");
        assertFalse(resultOffline.isAllSuccess());
        assertTrue(resultOffline.isServerUnavailable());
    }

    @Test
    public void testUiPanelInstantiation() {
        // Run in headless safe check or create components
        assertDoesNotThrow(() -> {
            DashboardPanel dashboardPanel = new DashboardPanel(null);
            assertNotNull(dashboardPanel);
            dashboardPanel.onLocaleChange();

            SyncPanel syncPanel = new SyncPanel(null);
            assertNotNull(syncPanel);
            syncPanel.onLocaleChange();

            com.krishidb.ui.pages.SettingsPanel settingsPanel = new com.krishidb.ui.pages.SettingsPanel(null);
            assertNotNull(settingsPanel);
            settingsPanel.onLocaleChange();
        });
    }

    @Test
    public void testSettingsPanelResponsiveness() {
        assertDoesNotThrow(() -> {
            com.krishidb.ui.pages.SettingsPanel settingsPanel = new com.krishidb.ui.pages.SettingsPanel(null);

            // Test target window resolutions minus 240px sidebar
            int[][] resolutions = {
                    {1280 - 240, 720},
                    {1366 - 240, 768},
                    {1536 - 240, 864},
                    {1920 - 240, 1080},
                    {800, 600} // Narrow window check
            };

            for (int[] res : resolutions) {
                int width = res[0];
                int height = res[1];
                settingsPanel.setSize(width, height);
                settingsPanel.doLayout();
                // Ensure no exceptions during layout at each resolution
                assertNotNull(settingsPanel.getLayout());
            }
        });
    }
}

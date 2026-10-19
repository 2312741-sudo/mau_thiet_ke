package vn.edu.dlu.dhopm.pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.dlu.dhopm.core.DatasetLoader;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.observer.AuditLogObserver;
import vn.edu.dlu.dhopm.pattern.observer.BenchmarkMetricsObserver;
import vn.edu.dlu.dhopm.pattern.observer.MiningObserver;
import vn.edu.dlu.dhopm.pattern.template.DefaultStreamMiner;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động kiểm chứng <b>Template Method Pattern &amp; Observer Pattern (GoF)</b>.
 *
 * <p>Kiểm chứng:
 * 1. Khung xương bất biến 5 bước trong AbstractStreamMiner
 * 2. Cơ chế kiểm định batch tại validateBatch
 * 3. Cơ chế phát tín hiệu thông báo cho MiningObserver và StreamObserver
 * 4. Hoạt động của AuditLogObserver và BenchmarkMetricsObserver</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class TemplateMethodAndObserverPatternTest {

    @Test
    @DisplayName("Template Method - 5-Step Lifecycle: Thực thi chu kỳ và phát kết quả cho Observer")
    void testTemplateMethodExecutionCycle() {
        DefaultStreamMiner miner = new DefaultStreamMiner();
        AtomicBoolean observerNotified = new AtomicBoolean(false);

        // Đăng ký Observer
        miner.addObserver(result -> {
            observerNotified.set(true);
            assertNotNull(result);
            assertEquals(8, result.currentTL());
            assertEquals(8, result.batch().size());
            assertEquals(2, result.dhopPatterns().size());
        });

        // Kích hoạt Template Method: processBatchAndMine
        MiningConfig config = MiningConfig.defaultPaperConfig();
        MiningCycleResult cycleResult = miner.processBatchAndMine(DatasetLoader.getPaperDataset(), config);

        assertTrue(observerNotified.get(), "Bước 5 của Template Method phải thông báo cho Observer");
        assertNotNull(cycleResult);
        assertEquals(8, cycleResult.currentTL());
        assertTrue(cycleResult.getPruningRatio() > 50.0, "Tỷ lệ cắt tỉa DUBO phải > 50%");
    }

    @Test
    @DisplayName("Template Method - validateBatch: Từ chối giao dịch null hoặc rỗng")
    void testValidateBatchRejectsInvalidTransactions() {
        DefaultStreamMiner miner = new DefaultStreamMiner();

        List<Transaction> invalidBatch = Collections.singletonList(null);
        assertThrows(IllegalArgumentException.class,
                () -> miner.processBatchAndMine(invalidBatch, MiningConfig.defaultPaperConfig()),
                "Giao dịch null phải bị từ chối tại Bước 1 validateBatch");
    }

    @Test
    @DisplayName("Observer - AuditLogObserver & BenchmarkMetricsObserver: Nhận dữ liệu đa Observer")
    void testMultipleObserversNotification() {
        DefaultStreamMiner miner = new DefaultStreamMiner();
        AuditLogObserver auditObserver = new AuditLogObserver();
        BenchmarkMetricsObserver metricsObserver = new BenchmarkMetricsObserver();

        miner.addObserver(auditObserver);
        miner.addObserver(metricsObserver);

        MiningConfig config = MiningConfig.defaultPaperConfig();
        miner.processBatchAndMine(DatasetLoader.getPaperDataset(), config);

        // Kiểm tra Audit Observer đã ghi nhận
        assertFalse(auditObserver.getLogEntries().isEmpty());
        assertTrue(auditObserver.getLogEntries().get(0).contains("[MINING-AUDIT]"));

        // Kiểm tra Benchmark Metrics Observer đã ghi nhận
        assertEquals(1, metricsObserver.getTotalCycles());
        assertTrue(metricsObserver.getAverageExecutionTimeMs() > 0.0);
        assertTrue(metricsObserver.getAveragePruningRatio() > 0.0);
    }
}

package vn.edu.dlu.dhopm.pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.dlu.dhopm.core.DatasetLoader;
import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.decorator.BenchmarkMinerDecorator;
import vn.edu.dlu.dhopm.pattern.decorator.LoggingMinerDecorator;
import vn.edu.dlu.dhopm.pattern.template.DefaultStreamMiner;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động kiểm chứng <b>Decorator Pattern (GoF)</b>.
 *
 * <p>Kiểm chứng:
 * 1. BenchmarkMinerDecorator đo lường thời gian thực thi (nanoseconds/microseconds) chính xác
 * 2. LoggingMinerDecorator ghi nhận nhật ký kiểm toán mà không xâm phạm logic thuật toán lõi
 * 3. Khả năng xếp chồng (Chaining) linh hoạt các Decorators quanh đối tượng StreamMiner</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class DecoratorPatternTest {

    @Test
    @DisplayName("Decorator - Benchmark: Đo lường thời gian thực thi > 0 ns và RAM tiêu thụ")
    void testBenchmarkMinerDecoratorMetrics() {
        DefaultStreamMiner baseMiner = new DefaultStreamMiner();
        BenchmarkMinerDecorator benchmarkDecorator = new BenchmarkMinerDecorator(baseMiner);

        MiningConfig config = MiningConfig.defaultPaperConfig();
        MiningCycleResult result = benchmarkDecorator.processBatchAndMine(DatasetLoader.getPaperDataset(), config);

        assertNotNull(result);
        assertEquals(8, result.currentTL());
        assertTrue(benchmarkDecorator.getLastExecutionTimeNs() > 0, "Thời gian thực thi nanoseconds phải > 0");
        assertTrue(benchmarkDecorator.getLastExecutionTimeMicros() > 0.0, "Thời gian thực thi microseconds phải > 0");
        assertEquals(1, benchmarkDecorator.getTotalRuns(), "Đã chạy đúng 1 chu kỳ");

        // Chạy thêm 1 lần nữa để kiểm tra thống kê trung bình
        benchmarkDecorator.mine(config);
        assertEquals(2, benchmarkDecorator.getTotalRuns());
        assertTrue(benchmarkDecorator.getAverageExecutionTimeMicros() > 0.0);

        // Kiểm tra số liệu đo lường Live Heap RAM (JVM Heap Telemetry)
        assertTrue(BenchmarkMinerDecorator.getCurrentHeapMemoryBytes() > 0, "Dung lượng heap bytes phải > 0");
        assertTrue(benchmarkDecorator.getCurrentHeapMemoryMb() > 0.0, "Dung lượng heap MB phải > 0");
        assertTrue(benchmarkDecorator.getTotalHeapMemoryMb() > 0.0, "Tổng heap MB phải > 0");
        assertTrue(benchmarkDecorator.getLastMemoryUsedBytes() > 0, "Lượng RAM cấp phát đo bởi decorator phải > 0");
        assertTrue(benchmarkDecorator.getLastMemoryUsedKb() > 0.0, "Lượng RAM cấp phát (KB) phải > 0");
        assertTrue(benchmarkDecorator.getPeakMemoryKb() > 0.0, "Đỉnh RAM chu kỳ phải > 0");
    }

    @Test
    @DisplayName("Decorator - Logging: Ghi vết quá trình xử lý batch và khai phá mẫu")
    void testLoggingMinerDecoratorOutput() {
        DefaultStreamMiner baseMiner = new DefaultStreamMiner();
        LoggingMinerDecorator loggingDecorator = new LoggingMinerDecorator(baseMiner);

        MiningConfig config = MiningConfig.defaultPaperConfig();
        loggingDecorator.processBatchAndMine(DatasetLoader.getPaperDataset(), config);

        List<String> logs = loggingDecorator.getExecutionLogs();
        assertFalse(logs.isEmpty(), "Danh sách log không được rỗng");
        assertTrue(logs.stream().anyMatch(log -> log.contains("Bắt đầu xử lý batch")), "Phải có log bắt đầu");
        assertTrue(logs.stream().anyMatch(log -> log.contains("Khai phá xong tại TL=8")), "Phải có log kết quả tại TL=8");
    }

    @Test
    @DisplayName("Decorator - Chaining: Xếp chồng Logging bọc ngoài Benchmark bọc ngoài Miner lõi")
    void testDecoratorChaining() {
        DefaultStreamMiner baseMiner = new DefaultStreamMiner();
        BenchmarkMinerDecorator benchmarkDecorator = new BenchmarkMinerDecorator(baseMiner);
        LoggingMinerDecorator chainedDecorator = new LoggingMinerDecorator(benchmarkDecorator);

        MiningConfig config = MiningConfig.defaultPaperConfig();
        MiningCycleResult result = chainedDecorator.processBatchAndMine(DatasetLoader.getPaperDataset(), config);

        // Kết quả vẫn phải chuẩn xác 100%
        assertEquals(8, chainedDecorator.getCurrentTL());
        assertEquals(8, chainedDecorator.getAllTransactions().size());
        assertEquals(2, result.dhopPatterns().size(), "Vẫn tìm được đúng 2 DHOPs chuẩn của bài báo");

        // Cả 2 decorator đều hoạt động độc lập
        assertTrue(benchmarkDecorator.getLastExecutionTimeNs() > 0, "Benchmark decorator phải ghi nhận thời gian");
        assertFalse(chainedDecorator.getExecutionLogs().isEmpty(), "Logging decorator phải ghi nhận logs");
    }
}

package vn.edu.dlu.dhopm.pattern.decorator;

import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.util.List;

/**
 * Concrete Decorator bổ sung tính năng đo đạc hiệu năng (Benchmarking) cho StreamMiner.
 *
 * <p>Áp dụng <b>Decorator Pattern (GoF)</b>:
 * Đo lường chính xác thời gian thực thi (nanoseconds / microseconds)
 * và lượng bộ nhớ Heap RAM biến thiên mà không can thiệp hay sửa đổi logic giải thuật lõi.</p>
 *
 * <p>Tuân thủ <b>Single Responsibility Principle (SRP)</b>:
 * Tách biệt hoàn toàn trách nhiệm đo lường số liệu ra khỏi trách nhiệm tính toán thuật toán.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class BenchmarkMinerDecorator extends AbstractMinerDecorator {

    private long lastExecutionTimeNs = 0;
    private long lastMemoryUsedBytes = 0;
    private long totalExecutionTimeNs = 0;
    private long totalRuns = 0;
    private long peakMemoryBytes = 0;

    public BenchmarkMinerDecorator(StreamMiner wrappedMiner) {
        super(wrappedMiner);
    }

    @Override
    public MiningCycleResult processBatchAndMine(List<Transaction> batch, MiningConfig config) {
        long startMem = getHeapMemoryUsed();
        long startNs = System.nanoTime();

        MiningCycleResult result = super.processBatchAndMine(batch, config);

        long elapsedNs = System.nanoTime() - startNs;
        long endMem = getHeapMemoryUsed();
        long memUsed = Math.max(0, endMem - startMem);

        recordBenchmark(elapsedNs, memUsed);

        // Tạo kết quả được tăng cường với thời gian đo được từ decorator
        return new MiningCycleResult(
                result.batch(),
                result.currentTL(),
                result.allVisitedPatterns(),
                result.dhopPatterns(),
                elapsedNs,
                memUsed
        );
    }

    @Override
    public List<PatternResult> mine(MiningConfig config) {
        long startMem = getHeapMemoryUsed();
        long startNs = System.nanoTime();

        List<PatternResult> results = super.mine(config);

        long elapsedNs = System.nanoTime() - startNs;
        long endMem = getHeapMemoryUsed();
        long memUsed = Math.max(0, endMem - startMem);

        recordBenchmark(elapsedNs, memUsed);

        return results;
    }

    private synchronized void recordBenchmark(long elapsedNs, long memUsed) {
        this.lastExecutionTimeNs = elapsedNs;
        this.lastMemoryUsedBytes = memUsed;
        this.totalExecutionTimeNs += elapsedNs;
        this.totalRuns++;
        if (memUsed > peakMemoryBytes) {
            this.peakMemoryBytes = memUsed;
        }
    }

    public synchronized long getLastExecutionTimeNs() {
        return lastExecutionTimeNs;
    }

    public synchronized double getLastExecutionTimeMicros() {
        return lastExecutionTimeNs / 1_000.0;
    }

    public synchronized double getLastExecutionTimeMs() {
        return lastExecutionTimeNs / 1_000_000.0;
    }

    public synchronized long getLastMemoryUsedBytes() {
        return lastMemoryUsedBytes;
    }

    public synchronized double getLastMemoryUsedKb() {
        return lastMemoryUsedBytes / 1024.0;
    }

    public synchronized double getAverageExecutionTimeMicros() {
        if (totalRuns == 0) return 0.0;
        return (totalExecutionTimeNs / (double) totalRuns) / 1_000.0;
    }

    public synchronized long getTotalRuns() {
        return totalRuns;
    }

    public synchronized long getPeakMemoryBytes() {
        return peakMemoryBytes;
    }

    private static long getHeapMemoryUsed() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }
}

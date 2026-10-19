package vn.edu.dlu.dhopm.pattern.decorator;

import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Concrete Decorator bổ sung tính năng đo đạc hiệu năng (Benchmarking) cho StreamMiner.
 *
 * <p>Áp dụng <b>Decorator Pattern (GoF)</b>:
 * Đo lường chính xác thời gian thực thi (nanoseconds / microseconds)
 * và lượng bộ nhớ Heap RAM tiêu thụ mà không can thiệp hay sửa đổi logic giải thuật lõi.</p>
 *
 * <p>Sử dụng <b>ThreadMXBean (JVM Management)</b> để đo chính xác từng byte RAM được cấp phát
 * (Thread Allocated Memory / TLAB), khắc phục hoàn toàn nhược điểm Runtime.freeMemory() bị làm tròn.</p>
 *
 * <p>Tuân thủ <b>Single Responsibility Principle (SRP)</b>:
 * Tách biệt hoàn toàn trách nhiệm đo lường số liệu ra khỏi trách nhiệm tính toán thuật toán.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class BenchmarkMinerDecorator extends AbstractMinerDecorator {

    private static final com.sun.management.ThreadMXBean THREAD_MX_BEAN;
    private static final boolean THREAD_ALLOC_SUPPORTED;

    static {
        com.sun.management.ThreadMXBean sunBean = null;
        boolean supported = false;
        try {
            java.lang.management.ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            if (bean instanceof com.sun.management.ThreadMXBean sb && sb.isThreadAllocatedMemorySupported()) {
                sunBean = sb;
                supported = true;
            }
        } catch (Throwable ignored) {
        }
        THREAD_MX_BEAN = sunBean;
        THREAD_ALLOC_SUPPORTED = supported;
    }

    private long lastExecutionTimeNs = 0;
    private long lastMemoryUsedBytes = 0;
    private long totalExecutionTimeNs = 0;
    private long totalRuns = 0;
    private long peakMemoryBytes = 0;

    public BenchmarkMinerDecorator(StreamMiner wrappedMiner) {
        super(wrappedMiner);
    }

    /**
     * Đo lường chính xác lượng bộ nhớ Heap được cấp phát bởi luồng hiện tại (Thread Allocated Memory).
     */
    public static long getThreadAllocatedBytes() {
        if (THREAD_ALLOC_SUPPORTED && THREAD_MX_BEAN != null) {
            try {
                return THREAD_MX_BEAN.getCurrentThreadAllocatedBytes();
            } catch (Throwable ignored) {
            }
        }
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    @Override
    public MiningCycleResult processBatchAndMine(List<Transaction> batch, MiningConfig config) {
        long startMem = getThreadAllocatedBytes();
        long startNs = System.nanoTime();

        MiningCycleResult result = super.processBatchAndMine(batch, config);

        long elapsedNs = System.nanoTime() - startNs;
        long endMem = getThreadAllocatedBytes();
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
        long startMem = getThreadAllocatedBytes();
        long startNs = System.nanoTime();

        List<PatternResult> results = super.mine(config);

        long elapsedNs = System.nanoTime() - startNs;
        long endMem = getThreadAllocatedBytes();
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

    public synchronized double getPeakMemoryKb() {
        return peakMemoryBytes / 1024.0;
    }

    /**
     * Lay dung luong bo nho Heap RAM hien tai dang su dung boi JVM (bytes).
     */
    public static long getCurrentHeapMemoryBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    /**
     * Lay dung luong bo nho Heap RAM hien tai dang su dung (MB).
     */
    public double getCurrentHeapMemoryMb() {
        return getCurrentHeapMemoryBytes() / (1024.0 * 1024.0);
    }

    /**
     * Lay tong dung luong bo nho Heap RAM duoc cap phat boi JVM (MB).
     */
    public double getTotalHeapMemoryMb() {
        return Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0);
    }

    private static long getHeapMemoryUsed() {
        return getCurrentHeapMemoryBytes();
    }
}

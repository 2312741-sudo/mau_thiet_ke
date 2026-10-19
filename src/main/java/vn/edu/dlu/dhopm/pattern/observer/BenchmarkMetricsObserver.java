package vn.edu.dlu.dhopm.pattern.observer;

import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Concrete Observer thu thập số liệu đo lường hiệu năng (Benchmark Telemetry).
 *
 * <p>Theo dõi thời gian thực thi, mức tiêu hao RAM và tỷ lệ cắt tỉa qua từng chu kỳ để phục vụ phân tích so sánh.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class BenchmarkMetricsObserver implements MiningObserver {

    private final List<MiningCycleResult> history = new ArrayList<>();

    @Override
    public synchronized void onMiningCompleted(MiningCycleResult result) {
        history.add(result);
    }

    public synchronized List<MiningCycleResult> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public synchronized double getAverageExecutionTimeMs() {
        if (history.isEmpty()) return 0.0;
        DoubleSummaryStatistics stats = history.stream()
                .mapToDouble(MiningCycleResult::getExecutionTimeMs)
                .summaryStatistics();
        return stats.getAverage();
    }

    public synchronized double getAveragePruningRatio() {
        if (history.isEmpty()) return 0.0;
        return history.stream()
                .mapToDouble(MiningCycleResult::getPruningRatio)
                .average()
                .orElse(0.0);
    }

    public synchronized int getTotalCycles() {
        return history.size();
    }

    public synchronized void clear() {
        history.clear();
    }
}

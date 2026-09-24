package vn.edu.dlu.dhopm.pattern.template;

import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.model.Transaction;

import java.util.Collections;
import java.util.List;

/**
 * Đóng gói toàn bộ kết quả của một chu kỳ khai phá dữ liệu luồng (Mining Cycle Result).
 *
 * @param batch               Danh sách các giao dịch vừa được nạp vào luồng
 * @param currentTL           Thời điểm logic hiện tại (Current Time Landmark)
 * @param allVisitedPatterns  Toàn bộ các mẫu ứng viên đã được giải thuật DFS duyệt qua
 * @param dhopPatterns        Danh sách các mẫu đạt ngưỡng độ chiếm dụng cao (DHOPs)
 * @param executionTimeNs     Thời gian thực thi của chu kỳ khai phá tính bằng nanoseconds
 * @param memoryUsedBytes     Dung lượng bộ nhớ RAM xấp xỉ được tiêu thụ tính bằng bytes
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public record MiningCycleResult(
        List<Transaction> batch,
        int currentTL,
        List<PatternResult> allVisitedPatterns,
        List<PatternResult> dhopPatterns,
        long executionTimeNs,
        long memoryUsedBytes
) {
    public MiningCycleResult {
        batch = batch == null ? Collections.emptyList() : Collections.unmodifiableList(batch);
        allVisitedPatterns = allVisitedPatterns == null ? Collections.emptyList() : Collections.unmodifiableList(allVisitedPatterns);
        dhopPatterns = dhopPatterns == null ? Collections.emptyList() : Collections.unmodifiableList(dhopPatterns);
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }

    public double getExecutionTimeMicros() {
        return executionTimeNs / 1_000.0;
    }

    public double getMemoryUsedKb() {
        return memoryUsedBytes / 1024.0;
    }

    public double getPruningRatio() {
        if (allVisitedPatterns.isEmpty()) return 0.0;
        long pruned = allVisitedPatterns.stream().filter(PatternResult::isPruned).count();
        return ((double) pruned / allVisitedPatterns.size()) * 100.0;
    }
}

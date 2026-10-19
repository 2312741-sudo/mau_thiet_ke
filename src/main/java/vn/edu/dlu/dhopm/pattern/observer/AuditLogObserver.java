package vn.edu.dlu.dhopm.pattern.observer;

import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Observer ghi lại nhật ký kiểm toán (Audit Trail) của toàn bộ hoạt động luồng và khai phá.
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class AuditLogObserver implements StreamObserver, MiningObserver {

    private final List<String> logEntries = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Override
    public synchronized void onBatchArrived(List<Transaction> newBatch, int currentTL) {
        String log = String.format("[%s] [STREAM-AUDIT] Batch cập bến: %d giao dịch, TL mới = T%d",
                LocalDateTime.now().format(FORMATTER), newBatch.size(), currentTL);
        logEntries.add(log);
        System.out.println(log);
    }

    @Override
    public synchronized void onMiningCompleted(MiningCycleResult result) {
        String log = String.format("[%s] [MINING-AUDIT] Hoàn tất khai phá: TL=T%d | Duyệt %d mẫu | DHOPs=%d | Cắt tỉa=%.1f%% | Thời gian=%.3f ms",
                LocalDateTime.now().format(FORMATTER),
                result.currentTL(),
                result.allVisitedPatterns().size(),
                result.dhopPatterns().size(),
                result.getPruningRatio(),
                result.getExecutionTimeMs());
        logEntries.add(log);
        System.out.println(log);
    }

    public synchronized List<String> getLogEntries() {
        return Collections.unmodifiableList(logEntries);
    }

    public synchronized void clear() {
        logEntries.clear();
    }
}

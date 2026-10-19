package vn.edu.dlu.dhopm.pattern.decorator;

import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Decorator bổ sung tính năng ghi nhật ký chi tiết (Logging) cho StreamMiner.
 *
 * <p>Theo dõi luồng xử lý của từng chu kỳ khai phá mà không làm biến đổi logic bên trong đối tượng được bọc.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class LoggingMinerDecorator extends AbstractMinerDecorator {

    private final List<String> executionLogs = new ArrayList<>();

    public LoggingMinerDecorator(StreamMiner wrappedMiner) {
        super(wrappedMiner);
    }

    @Override
    public MiningCycleResult processBatchAndMine(List<Transaction> batch, MiningConfig config) {
        log("Bắt đầu xử lý batch: " + (batch == null ? 0 : batch.size()) + " giao dịch. Cấu hình: " + config.getDecayStrategy().getName());
        MiningCycleResult result = super.processBatchAndMine(batch, config);
        log(String.format("Khai phá xong tại TL=%d: %d mẫu duyệt qua, %d DHOPs, tỉ lệ cắt tỉa: %.1f%%",
                result.currentTL(),
                result.allVisitedPatterns().size(),
                result.dhopPatterns().size(),
                result.getPruningRatio()));
        return result;
    }

    @Override
    public List<PatternResult> mine(MiningConfig config) {
        log("Thực hiện khai phá thủ công với chiến lược: " + config.getDecayStrategy().getName());
        List<PatternResult> results = super.mine(config);
        long dhopCount = results.stream().filter(PatternResult::isDHOP).count();
        log("Khai phá hoàn tất: " + results.size() + " mẫu duyệt qua, " + dhopCount + " DHOPs.");
        return results;
    }

    private synchronized void log(String msg) {
        String entry = "[StreamMiner-Logger] " + msg;
        executionLogs.add(entry);
        System.out.println(entry);
    }

    public synchronized List<String> getExecutionLogs() {
        return Collections.unmodifiableList(executionLogs);
    }
}

package vn.edu.dlu.dhopm.pattern.template;

import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.decorator.StreamMiner;
import vn.edu.dlu.dhopm.pattern.observer.MiningObserver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lớp trừu tượng định nghĩa khung xương giải thuật khai phá luồng
 * theo <b>Template Method Pattern (GoF)</b>.
 *
 * <p>Quy trình chuẩn hóa 5 bước bất biến:
 * <ol>
 *   <li><b>validateBatch:</b> Kiểm tra tính toàn vẹn và hợp lệ của lô giao dịch đến</li>
 *   <li><b>updateStructure:</b> Cập nhật cấu trúc tóm lược Global DHO-List (One-scan streaming)</li>
 *   <li><b>calculateBounds:</b> Tính toán lại trọng số suy giảm và xác lập thứ tự duyệt theo cận trên</li>
 *   <li><b>minePatterns:</b> Khai phá không gian mẫu tổ hợp bằng tìm kiếm theo chiều sâu (DFS)</li>
 *   <li><b>notifyObservers:</b> Phát sự kiện hoàn tất và truyền kết quả đến các bên quan sát (Observer)</li>
 * </ol>
 * </p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public abstract class AbstractStreamMiner implements StreamMiner {

    protected final List<MiningObserver> observers = new CopyOnWriteArrayList<>();

    public void addObserver(MiningObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public void removeObserver(MiningObserver observer) {
        observers.remove(observer);
    }

    /**
     * <b>Template Method (final)</b>: Định nghĩa khung xương cố định của chu kỳ khai phá.
     * Các lớp con chỉ được tùy biến nội dung từng bước cụ thể thông qua các primitive operations,
     * tuyệt đối không được phá vỡ trình tự 5 bước này.
     */
    @Override
    public final MiningCycleResult processBatchAndMine(List<Transaction> batch, MiningConfig config) {
        long startNs = System.nanoTime();
        long startMem = getUsedMemory();

        // Bước 1: Kiểm tra tính toàn vẹn của lô giao dịch
        validateBatch(batch);

        // Bước 2: Cập nhật cấu trúc dữ liệu tóm tắt DHO-List một lần duy nhất
        updateStructure(batch);

        // Bước 3: Tính toán lại trọng số suy giảm và cập nhật thứ tự xử lý
        calculateBounds(config);

        // Bước 4: Khai phá mẫu theo chiều sâu kết hợp chiến lược cắt tỉa
        List<PatternResult> allVisited = minePatterns(config);

        long executionTimeNs = System.nanoTime() - startNs;
        long endMem = getUsedMemory();
        long memUsedBytes = Math.max(0, endMem - startMem);

        List<PatternResult> dhops = allVisited.stream()
                .filter(PatternResult::isDHOP)
                .toList();

        MiningCycleResult result = new MiningCycleResult(
                batch,
                getCurrentTL(),
                allVisited,
                dhops,
                executionTimeNs,
                memUsedBytes
        );

        // Bước 5: Báo cáo kết quả cho các Observer
        notifyObservers(result);

        return result;
    }

    /**
     * Bước 1: Kiểm định tính hợp lệ của batch giao dịch.
     */
    protected abstract void validateBatch(List<Transaction> batch);

    /**
     * Bước 2: Cập nhật cấu trúc dữ liệu tóm lược (DHO-List).
     */
    protected abstract void updateStructure(List<Transaction> batch);

    /**
     * Bước 3: Tính toán lại trọng số suy giảm và cận trên (Bounds & Sorting).
     */
    protected abstract void calculateBounds(MiningConfig config);

    /**
     * Bước 4: Thực hiện khai phá không gian mẫu (DFS Pattern Growth).
     */
    protected abstract List<PatternResult> minePatterns(MiningConfig config);

    /**
     * Bước 5: Thông báo kết quả tới toàn bộ Observers.
     */
    protected void notifyObservers(MiningCycleResult result) {
        for (MiningObserver observer : observers) {
            try {
                observer.onMiningCompleted(result);
            } catch (Exception e) {
                System.err.println("Lỗi khi thông báo Observer: " + e.getMessage());
            }
        }
    }

    private static long getUsedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }
}

package vn.edu.dlu.dhopm.pattern.strategy;

/**
 * Concrete Strategy: Không cắt tỉa (No Pruning Strategy).
 *
 * <p>Duyệt toàn bộ không gian tổ hợp (Brute-force DFS traversal).
 * Dùng làm đường cơ sở (baseline) trong các thử nghiệm Benchmark
 * để chứng minh hiệu quả giảm không gian tìm kiếm vượt trội của cận trên DUBO.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class NoPruningStrategy implements PruningStrategy {

    @Override
    public boolean shouldPrune(double upperBound, double minSup) {
        // Luôn trả về false để duyệt trọn vẹn mọi nhánh
        return false;
    }

    @Override
    public String getName() {
        return "No Pruning (Exhaustive DFS)";
    }

    @Override
    public String getDescription() {
        return "Không cắt tỉa nhánh. Duyệt vét cạn để làm đối chứng hiệu năng (Baseline benchmark).";
    }

    @Override
    public String toString() {
        return getName();
    }
}

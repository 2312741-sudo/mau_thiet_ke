package vn.edu.dlu.dhopm.pattern.strategy;

/**
 * Concrete Strategy: Cắt tỉa nhánh tìm kiếm bằng cận trên DUBO (Định nghĩa 6 trong bài báo DHOPM).
 *
 * <p>Quy tắc: Nhánh mẫu X sẽ bị cắt tỉa nếu DUBO(X) &lt; minSup.
 * Vì tính chất đơn điệu của DUBO đảm bảo không có mẫu mở rộng nào của X có thể đạt minSup.</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class DUBOPruningStrategy implements PruningStrategy {

    private static final double EPSILON = 1e-9;

    @Override
    public boolean shouldPrune(double upperBound, double minSup) {
        return upperBound < minSup - EPSILON;
    }

    @Override
    public String getName() {
        return "DUBO Upper Bound Pruning";
    }

    @Override
    public String getDescription() {
        return "Cắt tỉa an toàn bằng cận trên DUBO(X) < minSup theo bài báo gốc DHOPM.";
    }

    @Override
    public String toString() {
        return getName();
    }
}

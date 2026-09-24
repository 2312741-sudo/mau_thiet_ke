package vn.edu.dlu.dhopm.pattern.strategy;

/**
 * Concrete Strategy: Mô hình cửa sổ mốc (Landmark Window Model).
 *
 * <p>Đặc tính: Không suy giảm trọng số theo thời gian (tương đương f = 1.0).
 * Mọi giao dịch từ thời điểm mốc (bắt đầu stream) đến hiện tại đều có trọng số bằng 1.0.</p>
 *
 * <p>Công thức: <b>w(tid, TL) = 1.0</b></p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class LandmarkDecay implements DecayStrategy {

    public LandmarkDecay() {
    }

    @Override
    public double calculateDecay(int tid, int TL) {
        return 1.0;
    }

    @Override
    public String getName() {
        return "Landmark Window (f=1.0)";
    }

    @Override
    public String getDescription() {
        return "Mô hình mốc không suy giảm. Mọi giao dịch có trọng số bằng nhau = 1.0.";
    }

    @Override
    public double getParameter() {
        return 1.0;
    }

    @Override
    public String toString() {
        return getName();
    }
}

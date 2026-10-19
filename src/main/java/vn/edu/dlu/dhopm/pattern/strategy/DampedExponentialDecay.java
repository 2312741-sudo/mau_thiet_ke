package vn.edu.dlu.dhopm.pattern.strategy;

/**
 * Concrete Strategy: Mô hình suy giảm hàm mũ (Damped Exponential Decay) chuẩn bài báo khoa học DHOPM.
 *
 * <p>Công thức: <b>w(tid, TL) = f^(TL - tid)</b></p>
 * Trong đó:
 * <ul>
 *   <li>f: Hệ số suy giảm, 0 &lt; f &le; 1.0</li>
 *   <li>(TL - tid): Khoảng cách thời gian giao dịch so với giao dịch hiện tại</li>
 * </ul>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class DampedExponentialDecay implements DecayStrategy {

    private final double decayFactor;

    public DampedExponentialDecay(double decayFactor) {
        if (!Double.isFinite(decayFactor) || decayFactor <= 0.0 || decayFactor > 1.0) {
            throw new IllegalArgumentException("Hệ số suy giảm f phải nằm trong khoảng (0, 1]. Nhận được: " + decayFactor);
        }
        this.decayFactor = decayFactor;
    }

    @Override
    public double calculateDecay(int tid, int TL) {
        if (tid > TL) {
            return 1.0;
        }
        return Math.pow(decayFactor, TL - tid);
    }

    @Override
    public String getName() {
        return "Damped Exponential (f=" + String.format("%.2f", decayFactor) + ")";
    }

    @Override
    public String getDescription() {
        return "Suy giảm hàm mũ f^(TL-tid). Dữ liệu cũ giảm giá trị theo cấp số nhân.";
    }

    @Override
    public double getParameter() {
        return decayFactor;
    }

    @Override
    public String toString() {
        return getName();
    }
}

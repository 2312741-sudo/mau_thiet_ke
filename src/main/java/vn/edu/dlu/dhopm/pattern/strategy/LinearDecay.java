package vn.edu.dlu.dhopm.pattern.strategy;

/**
 * Concrete Strategy: Mô hình suy giảm tuyến tính (Linear Decay).
 *
 * <p>Công thức: <b>w(tid, TL) = max(0.0, 1.0 - lambda * (TL - tid))</b></p>
 * Trong đó:
 * <ul>
 *   <li>lambda: Độ dốc suy giảm tuyến tính (lambda &gt; 0, thường từ 0.01 đến 0.1)</li>
 *   <li>(TL - tid): Số bước thời gian đã trôi qua</li>
 * </ul>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class LinearDecay implements DecayStrategy {

    private final double lambda;

    public LinearDecay(double lambda) {
        if (!Double.isFinite(lambda) || lambda <= 0.0 || lambda > 1.0) {
            throw new IllegalArgumentException("Hệ số suy giảm tuyến tính lambda phải nằm trong khoảng (0, 1]. Nhận được: " + lambda);
        }
        this.lambda = lambda;
    }

    @Override
    public double calculateDecay(int tid, int TL) {
        if (tid > TL) {
            return 1.0;
        }
        double decay = 1.0 - lambda * (TL - tid);
        return Math.max(0.0, decay);
    }

    @Override
    public String getName() {
        return "Linear Decay (λ=" + String.format("%.2f", lambda) + ")";
    }

    @Override
    public String getDescription() {
        return "Suy giảm tuyến tính max(0, 1 - λ*(TL-tid)). Trọng số giảm đều đặn theo thời gian.";
    }

    @Override
    public double getParameter() {
        return lambda;
    }

    @Override
    public String toString() {
        return getName();
    }
}

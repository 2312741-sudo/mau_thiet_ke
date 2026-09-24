package vn.edu.dlu.dhopm.core;

import vn.edu.dlu.dhopm.model.Entry;
import vn.edu.dlu.dhopm.pattern.strategy.DampedExponentialDecay;
import vn.edu.dlu.dhopm.pattern.strategy.DecayStrategy;

import java.util.*;

/**
 * Bộ tính toán cận trên Damped Upper Bound Occupancy (DUBO) theo Định nghĩa 6 trong bài báo DHOPM.
 *
 * <p>Tích hợp <b>Strategy Pattern</b> để hỗ trợ mọi mô hình suy giảm trọng số.</p>
 *
 * Công thức tổng quát:
 *   DUBO(X, k) = sum_{i=k}^{u} ( n_i * (l_k / l_i) * w(T_k, TL) )
 *   DUBO(X) = max_{1 <= k <= u} { DUBO(X, k) }
 *
 * Trong đó:
 * - L = {l_1, l_2, ..., l_u} là tập độ dài các giao dịch chứa X (sắp tăng dần)
 * - n_k là số lượng giao dịch có độ dài l_k
 * - T_k là TID lớn nhất trong các giao dịch có độ dài l_k chứa X
 * - TL là TID mới nhất của toàn bộ CSDL đã quét
 * - w(T_k, TL) là trọng số suy giảm do DecayStrategy tính toán
 */
public class DUBOCalculator {

    /**
     * Tính toán DUBO cho một tập hợp các entry &lt;TID, TLen&gt; sử dụng DecayStrategy (Strategy Pattern).
     *
     * @param entries  Danh sách các entry của mẫu X
     * @param strategy Chiến lược suy giảm trọng số
     * @param TL       TID mới nhất trong CSDL
     * @return Giá trị cận trên DUBO(X)
     */
    public static double calculate(List<Entry> entries, DecayStrategy strategy, int TL) {
        if (strategy == null) {
            throw new IllegalArgumentException("DecayStrategy must not be null");
        }
        if (entries == null || entries.isEmpty()) {
            return 0.0;
        }

        // 1. Gom nhóm các entry theo độ dài giao dịch (tlen)
        Map<Integer, List<Integer>> lengthToTids = new TreeMap<>();
        for (Entry e : entries) {
            lengthToTids.computeIfAbsent(e.tlen(), k -> new ArrayList<>()).add(e.tid());
        }

        List<Integer> sortedLens = new ArrayList<>(lengthToTids.keySet());
        int u = sortedLens.size();

        double maxDUBO = 0.0;

        // 2. Tính toán DUBO(X, k) cho mỗi k từ 0 đến u-1
        for (int k = 0; k < u; k++) {
            int lk = sortedLens.get(k);
            List<Integer> tidsOfLk = lengthToTids.get(lk);
            int Tk = Collections.max(tidsOfLk); // TID lớn nhất trong các GD có độ dài l_k

            double sum = 0.0;
            for (int i = k; i < u; i++) {
                int li = sortedLens.get(i);
                int ni = lengthToTids.get(li).size();
                sum += ni * ((double) lk / li);
            }

            double decay = strategy.calculateDecay(Tk, TL);
            double duboK = sum * decay;

            if (duboK > maxDUBO) {
                maxDUBO = duboK;
            }
        }

        return maxDUBO;
    }

    /**
     * Tính toán DUBO cho một tập hợp các entry &lt;TID, TLen&gt; (Tương thích ngược với bài báo gốc f).
     *
     * @param entries Danh sách các entry của mẫu X
     * @param f       Hệ số suy giảm (0 &lt; f &le; 1)
     * @param TL      TID mới nhất trong CSDL
     * @return Giá trị cận trên DUBO(X)
     */
    public static double calculate(List<Entry> entries, double f, int TL) {
        if (!Double.isFinite(f) || f <= 0.0 || f > 1.0) {
            throw new IllegalArgumentException("Decay factor f must be in (0, 1]");
        }
        return calculate(entries, new DampedExponentialDecay(f), TL);
    }
}

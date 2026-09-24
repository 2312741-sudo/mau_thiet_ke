package vn.edu.dlu.dhopm.pattern.builder;

import vn.edu.dlu.dhopm.pattern.strategy.DampedExponentialDecay;
import vn.edu.dlu.dhopm.pattern.strategy.DUBOPruningStrategy;
import vn.edu.dlu.dhopm.pattern.strategy.DecayStrategy;
import vn.edu.dlu.dhopm.pattern.strategy.PruningStrategy;

import java.util.Objects;

/**
 * Lớp cấu hình bất biến (Immutable Configuration) cho quy trình khai phá dữ liệu luồng DHOPM.
 *
 * <p>Áp dụng <b>Builder Pattern (GoF)</b>:
 * Cho phép thiết lập tham số thuật toán một cách linh hoạt, an toàn (Type-safe),
 * tự kiểm tra tính hợp lệ (Validation) tại thời điểm khởi tạo,
 * đồng thời loại bỏ lỗi truyền sai thứ tự tham số (Telescoping Constructor Anti-pattern).</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public final class MiningConfig {

    private final double decayFactor;
    private final double minSupRatio;
    private final Double customAbsoluteMinSup;
    private final DecayStrategy decayStrategy;
    private final PruningStrategy pruningStrategy;
    private final long streamBatchPeriodMs;
    private final int maxPatternLength;
    private final boolean autoReconstruct;

    private MiningConfig(Builder builder) {
        this.decayFactor = builder.decayFactor;
        this.minSupRatio = builder.minSupRatio;
        this.customAbsoluteMinSup = builder.customAbsoluteMinSup;
        this.decayStrategy = builder.decayStrategy != null 
                ? builder.decayStrategy 
                : new DampedExponentialDecay(builder.decayFactor);
        this.pruningStrategy = builder.pruningStrategy != null 
                ? builder.pruningStrategy 
                : new DUBOPruningStrategy();
        this.streamBatchPeriodMs = builder.streamBatchPeriodMs;
        this.maxPatternLength = builder.maxPatternLength;
        this.autoReconstruct = builder.autoReconstruct;
    }

    public double getDecayFactor() {
        return decayFactor;
    }

    public double getMinSupRatio() {
        return minSupRatio;
    }

    public Double getCustomAbsoluteMinSup() {
        return customAbsoluteMinSup;
    }

    /**
     * Tính toán minSup tuyệt đối dựa trên tổng số giao dịch đã nạp.
     * Nếu người dùng có đặt minSup thủ công thì ưu tiên sử dụng.
     */
    public double resolveAbsoluteMinSup(int totalTransactions) {
        if (customAbsoluteMinSup != null && customAbsoluteMinSup > 0) {
            return customAbsoluteMinSup;
        }
        return Math.max(0.0, totalTransactions * minSupRatio);
    }

    public DecayStrategy getDecayStrategy() {
        return decayStrategy;
    }

    public PruningStrategy getPruningStrategy() {
        return pruningStrategy;
    }

    public long getStreamBatchPeriodMs() {
        return streamBatchPeriodMs;
    }

    public int getMaxPatternLength() {
        return maxPatternLength;
    }

    public boolean isAutoReconstruct() {
        return autoReconstruct;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MiningConfig defaultPaperConfig() {
        return builder()
                .decayFactor(0.90)
                .minSupRatio(0.15)
                .streamBatchPeriodMs(1500)
                .build();
    }

    /**
     * Builder class hỗ trợ tạo đối tượng MiningConfig theo phương thức chuỗi (Fluent API).
     */
    public static final class Builder {
        private double decayFactor = 0.90;
        private double minSupRatio = 0.15;
        private Double customAbsoluteMinSup = null;
        private DecayStrategy decayStrategy = null;
        private PruningStrategy pruningStrategy = null;
        private long streamBatchPeriodMs = 1500;
        private int maxPatternLength = Integer.MAX_VALUE;
        private boolean autoReconstruct = true;

        public Builder decayFactor(double f) {
            if (!Double.isFinite(f) || f <= 0.0 || f > 1.0) {
                throw new IllegalArgumentException("Decay factor f must be in (0, 1]. Received: " + f);
            }
            this.decayFactor = f;
            return this;
        }

        public Builder minSupRatio(double ratio) {
            if (!Double.isFinite(ratio) || ratio <= 0.0 || ratio > 1.0) {
                throw new IllegalArgumentException("minSup ratio must be in (0, 1]. Received: " + ratio);
            }
            this.minSupRatio = ratio;
            return this;
        }

        public Builder absoluteMinSup(double minSup) {
            if (!Double.isFinite(minSup) || minSup < 0.0) {
                throw new IllegalArgumentException("absoluteMinSup must be >= 0. Received: " + minSup);
            }
            this.customAbsoluteMinSup = minSup;
            return this;
        }

        public Builder decayStrategy(DecayStrategy strategy) {
            this.decayStrategy = Objects.requireNonNull(strategy, "decayStrategy must not be null");
            return this;
        }

        public Builder pruningStrategy(PruningStrategy strategy) {
            this.pruningStrategy = Objects.requireNonNull(strategy, "pruningStrategy must not be null");
            return this;
        }

        public Builder streamBatchPeriodMs(long ms) {
            if (ms < 50) {
                throw new IllegalArgumentException("streamBatchPeriodMs must be >= 50ms. Received: " + ms);
            }
            this.streamBatchPeriodMs = ms;
            return this;
        }

        public Builder maxPatternLength(int len) {
            if (len <= 0) {
                throw new IllegalArgumentException("maxPatternLength must be > 0. Received: " + len);
            }
            this.maxPatternLength = len;
            return this;
        }

        public Builder autoReconstruct(boolean auto) {
            this.autoReconstruct = auto;
            return this;
        }

        public MiningConfig build() {
            return new MiningConfig(this);
        }
    }
}

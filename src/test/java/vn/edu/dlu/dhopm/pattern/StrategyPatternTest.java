package vn.edu.dlu.dhopm.pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.dlu.dhopm.core.DatasetLoader;
import vn.edu.dlu.dhopm.core.DHOPMEngine;
import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.strategy.*;
import vn.edu.dlu.dhopm.pattern.template.DefaultStreamMiner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động kiểm chứng <b>Strategy Pattern (GoF)</b>.
 *
 * <p>Kiểm chứng:
 * 1. Tính toán suy giảm trọng số theo hàm mũ (Damped Exponential)
 * 2. Tính toán suy giảm theo cửa sổ mốc (Landmark Window)
 * 3. Tính toán suy giảm tuyến tính (Linear Decay)
 * 4. Tính năng cắt tỉa DUBO Upper Bound vs No Pruning
 * 5. Khả năng hoán đổi chiến lược tại runtime mà không sửa đổi thuật toán lõi</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class StrategyPatternTest {

    @Test
    @DisplayName("Strategy - Exponential: w(tid, TL) = f^(TL - tid)")
    void testDampedExponentialDecayCalculations() {
        DecayStrategy strategy = new DampedExponentialDecay(0.9);

        assertEquals(1.0, strategy.calculateDecay(8, 8), 1e-9, "Khi tid == TL, decay = 0.9^0 = 1.0");
        assertEquals(0.9, strategy.calculateDecay(7, 8), 1e-9, "Khi TL-tid = 1, decay = 0.9^1 = 0.9");
        assertEquals(0.81, strategy.calculateDecay(6, 8), 1e-9, "Khi TL-tid = 2, decay = 0.9^2 = 0.81");
        assertEquals(1.0, strategy.calculateDecay(9, 8), 1e-9, "Khi tid > TL, decay = 1.0");

        assertThrows(IllegalArgumentException.class, () -> new DampedExponentialDecay(0.0));
        assertThrows(IllegalArgumentException.class, () -> new DampedExponentialDecay(1.1));
    }

    @Test
    @DisplayName("Strategy - Landmark Window: Mọi giao dịch có trọng số = 1.0")
    void testLandmarkDecayCalculations() {
        DecayStrategy strategy = new LandmarkDecay();

        assertEquals(1.0, strategy.calculateDecay(1, 8));
        assertEquals(1.0, strategy.calculateDecay(5, 8));
        assertEquals(1.0, strategy.calculateDecay(8, 8));
        assertEquals(1.0, strategy.getParameter());
    }

    @Test
    @DisplayName("Strategy - Linear Decay: w(tid, TL) = max(0, 1 - lambda*(TL - tid))")
    void testLinearDecayCalculations() {
        DecayStrategy strategy = new LinearDecay(0.1);

        assertEquals(1.0, strategy.calculateDecay(8, 8), 1e-9);
        assertEquals(0.9, strategy.calculateDecay(7, 8), 1e-9);
        assertEquals(0.5, strategy.calculateDecay(3, 8), 1e-9);
        assertEquals(0.0, strategy.calculateDecay(0, 15), 1e-9, "Khi khoảng cách quá lớn, suy giảm về 0.0");
    }

    @Test
    @DisplayName("Strategy - Pruning: DUBOPruningStrategy cắt tỉa đúng khi upperBound < minSup")
    void testDUBOPruningStrategy() {
        PruningStrategy duboPruning = new DUBOPruningStrategy();

        assertTrue(duboPruning.shouldPrune(1.19, 1.20), "1.19 < 1.20 nên phải bị cắt tỉa");
        assertFalse(duboPruning.shouldPrune(1.20, 1.20), "1.20 == minSup nên không bị cắt tỉa");
        assertFalse(duboPruning.shouldPrune(1.50, 1.20), "1.50 > 1.20 nên không bị cắt tỉa");
    }

    @Test
    @DisplayName("Strategy - Pruning: NoPruningStrategy duyệt toàn bộ không gian DFS (Baseline)")
    void testNoPruningStrategy() {
        PruningStrategy noPruning = new NoPruningStrategy();

        assertFalse(noPruning.shouldPrune(0.1, 1.2), "NoPruning luôn trả về false");
        assertFalse(noPruning.shouldPrune(0.0, 10.0), "NoPruning luôn trả về false");
    }

    @Test
    @DisplayName("Strategy - Runtime Swapping: So sánh số lượng DHOP giữa Damped và Landmark")
    void testRuntimeStrategySwapping() {
        DefaultStreamMiner miner = new DefaultStreamMiner();
        miner.processBatchAndMine(DatasetLoader.getPaperDataset(), MiningConfig.defaultPaperConfig());

        // 1. Chạy với Damped Exponential (f=0.9, minSup=1.2) -> 2 DHOPs: {AE, F}
        MiningConfig dampedConfig = MiningConfig.builder()
                .decayStrategy(new DampedExponentialDecay(0.9))
                .absoluteMinSup(1.2)
                .build();
        List<PatternResult> dampedResults = miner.mine(dampedConfig);
        long dampedDhopCount = dampedResults.stream().filter(PatternResult::isDHOP).count();
        assertEquals(2, dampedDhopCount, "Damped Exponential f=0.9 phải tìm được đúng 2 DHOPs");

        // 2. Hoán đổi sang Landmark Window (f=1.0, minSup=1.2) -> 9 DHOPs
        MiningConfig landmarkConfig = MiningConfig.builder()
                .decayStrategy(new LandmarkDecay())
                .absoluteMinSup(1.2)
                .build();
        List<PatternResult> landmarkResults = miner.mine(landmarkConfig);
        long landmarkDhopCount = landmarkResults.stream().filter(PatternResult::isDHOP).count();
        assertEquals(9, landmarkDhopCount, "Landmark Window f=1.0 phải tìm được đúng 9 DHOPs (khớp Lab 1 TC5)");

        // 3. Hoán đổi sang NoPruningStrategy: Số mẫu DHOP vẫn đúng nhưng tổng số mẫu duyệt lớn hơn hoặc bằng
        MiningConfig noPruneConfig = MiningConfig.builder()
                .decayStrategy(new DampedExponentialDecay(0.9))
                .pruningStrategy(new NoPruningStrategy())
                .absoluteMinSup(1.2)
                .build();
        List<PatternResult> noPruneResults = miner.mine(noPruneConfig);
        assertTrue(noPruneResults.size() >= dampedResults.size(),
                "NoPruning phải duyệt qua số lượng ứng viên lớn hơn hoặc bằng DUBO Pruning");
    }
}

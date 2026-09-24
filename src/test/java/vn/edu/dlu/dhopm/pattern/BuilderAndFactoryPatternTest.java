package vn.edu.dlu.dhopm.pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.factory.*;
import vn.edu.dlu.dhopm.pattern.strategy.LinearDecay;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động kiểm chứng <b>Builder Pattern &amp; Factory Method Pattern (GoF)</b>.
 *
 * <p>Kiểm chứng:
 * 1. Khởi tạo cấu hình an toàn bằng MiningConfig.Builder với đầy đủ kiểm tra ràng buộc
 * 2. Đa hình trong việc nạp dữ liệu từ các Factory cụ thể (Paper, DB0, Synthetic, CSV, Lab 1 Custom)</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class BuilderAndFactoryPatternTest {

    @Test
    @DisplayName("Builder - MiningConfig.Builder: Khởi tạo giá trị hợp lệ và từ chối tham số sai")
    void testMiningConfigBuilderValidation() {
        // Cấu hình hợp lệ
        MiningConfig config = MiningConfig.builder()
                .decayFactor(0.85)
                .minSupRatio(0.20)
                .decayStrategy(new LinearDecay(0.05))
                .streamBatchPeriodMs(2000)
                .build();

        assertNotNull(config);
        assertEquals(0.85, config.getDecayFactor());
        assertEquals(0.20, config.getMinSupRatio());
        assertEquals(2000, config.getStreamBatchPeriodMs());
        assertTrue(config.getDecayStrategy() instanceof LinearDecay);

        // Kiểm tra validation từ chối tham số không hợp lệ
        assertThrows(IllegalArgumentException.class, () -> MiningConfig.builder().decayFactor(0.0).build());
        assertThrows(IllegalArgumentException.class, () -> MiningConfig.builder().decayFactor(1.1).build());
        assertThrows(IllegalArgumentException.class, () -> MiningConfig.builder().minSupRatio(-0.1).build());
        assertThrows(IllegalArgumentException.class, () -> MiningConfig.builder().streamBatchPeriodMs(10).build());
    }

    @Test
    @DisplayName("Factory Method - DatasetLoaderFactory: Đa hình nạp dữ liệu từ các nguồn khác nhau")
    void testDatasetLoaderFactories() {
        // 1. PaperDatasetFactory: 8 giao dịch
        DatasetLoaderFactory paperFactory = new PaperDatasetFactory();
        List<Transaction> paperData = paperFactory.createDataset();
        assertEquals(8, paperData.size(), "PaperDatasetFactory phải sinh 8 giao dịch");
        assertEquals(1, paperData.get(0).getTid());

        // 2. DB0DatasetFactory: 4 giao dịch (T1..T4)
        DatasetLoaderFactory db0Factory = new DB0DatasetFactory();
        List<Transaction> db0Data = db0Factory.createDataset();
        assertEquals(4, db0Data.size());
        assertEquals(4, db0Data.get(3).getTid());

        // 3. SyntheticDatasetFactory: sinh ngẫu nhiên số lượng chỉ định
        DatasetLoaderFactory syntheticFactory = new SyntheticDatasetFactory(25, 100);
        List<Transaction> synthData = syntheticFactory.createDataset();
        assertEquals(25, synthData.size(), "SyntheticFactory phải sinh đúng 25 giao dịch");
        assertEquals(100, synthData.get(0).getTid());

        // 4. CustomLab1DatasetFactory: 10 giao dịch
        DatasetLoaderFactory lab1Factory = new CustomLab1DatasetFactory();
        List<Transaction> lab1Data = lab1Factory.createDataset();
        assertEquals(10, lab1Data.size());

        // 5. CsvDatasetFactory: đọc từ chuỗi định dạng
        String csvContent = "1 A B C\n2 B C D\n3 A D\n";
        DatasetLoaderFactory csvFactory = new CsvDatasetFactory(csvContent, "In-Memory CSV");
        List<Transaction> csvData = csvFactory.createDataset();
        assertEquals(3, csvData.size(), "CsvFactory phải đọc được 3 giao dịch");
        assertEquals(1, csvData.get(0).getTid());
        assertEquals(List.of("A", "B", "C"), csvData.get(0).getItems());
    }
}

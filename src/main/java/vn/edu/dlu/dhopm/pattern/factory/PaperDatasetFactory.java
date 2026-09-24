package vn.edu.dlu.dhopm.pattern.factory;

import vn.edu.dlu.dhopm.core.DatasetLoader;
import vn.edu.dlu.dhopm.model.Transaction;

import java.util.List;

/**
 * Concrete Creator nạp tập dữ liệu chuẩn 8 giao dịch từ bài báo khoa học DHOPM (EAAI 2026, Table 1).
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class PaperDatasetFactory extends DatasetLoaderFactory {

    @Override
    public List<Transaction> createDataset() {
        return DatasetLoader.getPaperDataset();
    }

    @Override
    public String getDatasetName() {
        return "Tập dữ liệu chuẩn bài báo (8 Giao dịch T1..T8)";
    }

    @Override
    public String getDatasetDescription() {
        return "Bao gồm 8 giao dịch mẫu (Table 1) dùng để thẩm định tính đúng đắn của giải thuật DHOPM.";
    }
}

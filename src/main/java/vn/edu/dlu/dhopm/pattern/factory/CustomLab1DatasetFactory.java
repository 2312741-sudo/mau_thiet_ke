package vn.edu.dlu.dhopm.pattern.factory;

import vn.edu.dlu.dhopm.core.DatasetLoader;
import vn.edu.dlu.dhopm.model.Transaction;

import java.util.List;

/**
 * Concrete Creator nạp tập dữ liệu kiểm thử tùy biến TC7_Custom (10 giao dịch) từ Lab 1.
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class CustomLab1DatasetFactory extends DatasetLoaderFactory {

    @Override
    public List<Transaction> createDataset() {
        return DatasetLoader.getTC7CustomDataset();
    }

    @Override
    public String getDatasetName() {
        return "Tập dữ liệu TC7 Lab 1 (10 Giao dịch)";
    }

    @Override
    public String getDatasetDescription() {
        return "Tập dữ liệu 10 giao dịch dùng để kiểm chứng tính toán bằng tay trong file Excel Lab 1.";
    }
}

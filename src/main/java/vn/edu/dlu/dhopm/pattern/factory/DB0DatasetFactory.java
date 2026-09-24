package vn.edu.dlu.dhopm.pattern.factory;

import vn.edu.dlu.dhopm.core.DatasetLoader;
import vn.edu.dlu.dhopm.model.Transaction;

import java.util.List;

/**
 * Concrete Creator nạp tập dữ liệu khởi tạo ban đầu DB0 (T1..T4) của bài báo DHOPM.
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class DB0DatasetFactory extends DatasetLoaderFactory {

    @Override
    public List<Transaction> createDataset() {
        return DatasetLoader.getDB0Dataset();
    }

    @Override
    public String getDatasetName() {
        return "Cơ sở dữ liệu khởi tạo DB0 (4 Giao dịch T1..T4)";
    }

    @Override
    public String getDatasetDescription() {
        return "Bao gồm 4 giao dịch ban đầu (Ví dụ 1 trong bài báo) để kiểm tra tính năng gia tăng theo luồng.";
    }
}

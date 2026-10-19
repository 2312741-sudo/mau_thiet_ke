package vn.edu.dlu.dhopm.pattern.factory;

import vn.edu.dlu.dhopm.model.Transaction;
import java.util.List;

/**
 * Creator Abstract Class trong <b>Factory Method Pattern (GoF)</b>.
 *
 * <p>Định nghĩa phương thức nhà máy (Factory Method) {@link #createDataset()}
 * để tạo danh sách các giao dịch (Transactions) mà không cần gắn chặt mã nguồn
 * với cấu trúc lưu trữ cụ thể (Hard-coded array, file CSV, hay thuật toán sinh ngẫu nhiên).</p>
 *
 * <p>Tuân thủ <b>Open-Closed Principle (OCP)</b> và <b>Single Responsibility Principle (SRP)</b>:
 * Mỗi lớp con chịu trách nhiệm tải một định dạng dữ liệu cụ thể.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public abstract class DatasetLoaderFactory {

    /**
     * Factory Method tạo danh sách giao dịch.
     *
     * @return Danh sách các giao dịch đã được chuẩn hóa
     */
    public abstract List<Transaction> createDataset();

    /**
     * Tên nguồn dữ liệu đại diện.
     */
    public abstract String getDatasetName();

    /**
     * Mô tả chi tiết nguồn dữ liệu.
     */
    public abstract String getDatasetDescription();

    @Override
    public String toString() {
        return getDatasetName();
    }
}

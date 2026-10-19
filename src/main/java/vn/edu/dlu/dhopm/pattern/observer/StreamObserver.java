package vn.edu.dlu.dhopm.pattern.observer;

import vn.edu.dlu.dhopm.model.Transaction;
import java.util.List;

/**
 * Observer Interface lắng nghe sự kiện luồng dữ liệu (Data Stream Event).
 *
 * <p>Áp dụng <b>Observer Pattern (GoF)</b>:
 * Tách biệt bộ phát sinh luồng dữ liệu (Subject / Stream Simulator)
 * khỏi các thành phần tiêu thụ (UI, Logger, Benchmark Collector).</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public interface StreamObserver {

    /**
     * Được gọi khi có một lô (batch) giao dịch mới vừa cập bến qua luồng.
     *
     * @param newBatch  Danh sách các giao dịch trong lô mới
     * @param currentTL Mã định danh thời gian mới nhất (Time Landmark)
     */
    void onBatchArrived(List<Transaction> newBatch, int currentTL);
}

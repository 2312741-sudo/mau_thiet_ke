package vn.edu.dlu.dhopm.pattern.decorator;

import vn.edu.dlu.dhopm.model.DHOList;
import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.util.List;

/**
 * Component Interface trong <b>Decorator Pattern (GoF)</b>.
 *
 * <p>Định nghĩa hợp đồng chuẩn cho bộ khai phá dữ liệu luồng.
 * Cả lớp khai phá cụ thể (Concrete Component) và các lớp bọc trang trí (Decorators)
 * đều hiện thực interface này.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public interface StreamMiner {

    /**
     * Xử lý một batch giao dịch mới và thực hiện khai phá theo cấu hình.
     */
    MiningCycleResult processBatchAndMine(List<Transaction> batch, MiningConfig config);

    /**
     * Chỉ thực hiện khai phá lại trên cấu trúc dữ liệu hiện có mà không nạp thêm batch mới.
     */
    List<PatternResult> mine(MiningConfig config);

    /**
     * Truy xuất cấu trúc danh sách DHO-List toàn cục.
     */
    DHOList getDHOList();

    /**
     * Lấy TID mới nhất hiện tại.
     */
    int getCurrentTL();

    /**
     * Lấy toàn bộ danh sách giao dịch đã nạp vào luồng.
     */
    List<Transaction> getAllTransactions();

    /**
     * Thiết lập lại trạng thái ban đầu của bộ khai phá.
     */
    void reset();
}

package vn.edu.dlu.dhopm.pattern.observer;

import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

/**
 * Observer Interface lắng nghe sự kiện hoàn thành chu kỳ khai phá (Mining Completion Event).
 *
 * <p>Áp dụng <b>Observer Pattern (GoF)</b>:
 * Cho phép đăng ký không giới hạn các module quan sát kết quả khai phá mà không làm biến đổi mã nguồn thuật toán.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public interface MiningObserver {

    /**
     * Được kích hoạt ngay sau khi một chu kỳ khai phá dữ liệu luồng hoàn tất.
     *
     * @param result Đóng gói kết quả khai phá, mẫu DHOP, thời gian chạy và bộ nhớ
     */
    void onMiningCompleted(MiningCycleResult result);
}

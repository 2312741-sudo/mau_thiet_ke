package vn.edu.dlu.dhopm.pattern.strategy;

/**
 * Strategy Interface định nghĩa chiến lược suy giảm trọng số theo thời gian (Decay Strategy).
 *
 * <p>Áp dụng <b>Strategy Pattern (GoF)</b>:
 * Cho phép hoán đổi linh hoạt giữa các mô hình suy giảm khác nhau (Exponential, Landmark, Linear)
 * tại runtime mà không cần sửa đổi mã nguồn của thuật toán khai phá lõi.</p>
 *
 * <p>Tuân thủ <b>Open-Closed Principle (OCP)</b>:
 * Khi cần bổ sung mô hình suy giảm mới (ví dụ: Sigmoid Decay, Sliding Window Decay),
 * chỉ cần hiện thực interface này mà không tác động đến lớp Engine.</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public interface DecayStrategy {

    /**
     * Tính toán hệ số suy giảm cho một giao dịch có mã tid so với thời điểm hiện tại TL.
     *
     * @param tid Mã định danh giao dịch (Transaction ID)
     * @param TL  Mã định danh giao dịch mới nhất hiện tại (Time Landmark / Current TL)
     * @return Trọng số suy giảm nằm trong đoạn [0.0, 1.0]
     */
    double calculateDecay(int tid, int TL);

    /**
     * Tên hiển thị của chiến lược trên giao diện người dùng.
     */
    String getName();

    /**
     * Mô tả toán học và đặc tính của chiến lược.
     */
    String getDescription();

    /**
     * Tham số đại diện chính của chiến lược (ví dụ: hệ số f với exponential decay).
     */
    double getParameter();
}

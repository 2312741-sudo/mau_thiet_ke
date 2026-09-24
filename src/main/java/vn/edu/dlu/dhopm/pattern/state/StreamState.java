package vn.edu.dlu.dhopm.pattern.state;

/**
 * State Interface trong <b>State Pattern (GoF)</b>.
 *
 * <p>Định nghĩa các hành vi phụ thuộc trạng thái của bộ mô phỏng luồng dữ liệu (Data Stream Simulator).
 * Giúp loại bỏ hoàn toàn các cấu trúc rẽ nhánh if-else phức tạp để kiểm tra cờ (flags)
 * và tự động điều khiển trạng thái bật/tắt (enabled/disabled) của các nút bấm trên giao diện người dùng.</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public interface StreamState {

    /**
     * Hành vi bắt đầu hoặc tiếp tục chạy luồng tự động.
     */
    void start(StreamStateContext context, long periodMs);

    /**
     * Hành vi tạm dừng luồng.
     */
    void pause(StreamStateContext context);

    /**
     * Hành vi bơm 1 bước đơn lẻ tiếp theo.
     */
    void step(StreamStateContext context);

    /**
     * Hành vi thiết lập lại (Reset) hệ thống về trạng thái ban đầu.
     */
    void reset(StreamStateContext context);

    /**
     * Tên trạng thái phục vụ hiển thị trên UI.
     */
    String getStateName();

    /**
     * Thông điệp trạng thái chi tiết.
     */
    String getStatusMessage();

    /**
     * Mã màu CSS tương ứng với trạng thái.
     */
    String getStatusColor();

    boolean isStartAllowed();

    boolean isPauseAllowed();

    boolean isStepAllowed();

    boolean isResetAllowed();
}

package vn.edu.dlu.dhopm.pattern.state;

/**
 * Context Interface trong <b>State Pattern (GoF)</b>.
 *
 * <p>Cung cấp các phương thức để các đối tượng State cụ thể tương tác và chuyển đổi trạng thái.</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public interface StreamStateContext {

    /**
     * Chuyển đổi sang trạng thái mới.
     */
    void setState(StreamState newState);

    /**
     * Lấy trạng thái hiện tại.
     */
    StreamState getState();

    /**
     * Thao tác khởi động bộ timer chạy luồng ngầm.
     */
    void doStartStream(long periodMs);

    /**
     * Thao tác tắt timer tạm dừng luồng ngầm.
     */
    void doPauseStream();

    /**
     * Thao tác thực thi bơm 1 bước đơn lẻ.
     */
    void doStepStream();

    /**
     * Thao tác đặt lại toàn bộ hệ thống.
     */
    void doResetStream();

    /**
     * Kiểm tra xem trong hàng đợi còn dữ liệu để bơm tiếp hay không.
     */
    boolean hasMoreData();
}

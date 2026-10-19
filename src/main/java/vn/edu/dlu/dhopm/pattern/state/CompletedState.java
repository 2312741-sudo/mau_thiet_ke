package vn.edu.dlu.dhopm.pattern.state;

/**
 * Concrete State: Trạng thái Hoàn tất luồng (Completed).
 *
 * <p>Toàn bộ các giao dịch trong hàng đợi mô phỏng đã được xử lý hết.
 * Khóa các nút Bắt đầu, Tạm dừng và Bước tiếp theo.
 * Chỉ cho phép người dùng bấm Khôi phục (Reset) để nạp lại nguồn dữ liệu mới.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class CompletedState implements StreamState {

    @Override
    public void start(StreamStateContext context, long periodMs) {
        // Đã hết dữ liệu trong hàng đợi
    }

    @Override
    public void pause(StreamStateContext context) {
        // Luồng đã kết thúc
    }

    @Override
    public void step(StreamStateContext context) {
        // Không còn giao dịch để bơm
    }

    @Override
    public void reset(StreamStateContext context) {
        context.doResetStream();
        context.setState(new IdleState());
    }

    @Override
    public String getStateName() {
        return "COMPLETED";
    }

    @Override
    public String getStatusMessage() {
        return "Hoàn tất luồng ✓";
    }

    @Override
    public String getStatusColor() {
        return "#6b7280"; // Gray
    }

    @Override
    public boolean isStartAllowed() {
        return false;
    }

    @Override
    public boolean isPauseAllowed() {
        return false;
    }

    @Override
    public boolean isStepAllowed() {
        return false;
    }

    @Override
    public boolean isResetAllowed() {
        return true;
    }
}

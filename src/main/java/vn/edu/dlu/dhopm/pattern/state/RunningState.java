package vn.edu.dlu.dhopm.pattern.state;

/**
 * Concrete State: Trạng thái Luồng đang chạy tự động (Running).
 *
 * <p>Tiến trình ngầm đang liên tục bơm giao dịch vào hệ thống theo chu kỳ.
 * Vô hiệu hóa nút Bắt đầu, Bước tiếp theo và Khôi phục để tránh tương tranh dữ liệu.
 * Chỉ cho phép bấm nút Tạm dừng (Pause).</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class RunningState implements StreamState {

    @Override
    public void start(StreamStateContext context, long periodMs) {
        // Đang chạy rồi, không làm gì
    }

    @Override
    public void pause(StreamStateContext context) {
        context.doPauseStream();
        context.setState(new PausedState());
    }

    @Override
    public void step(StreamStateContext context) {
        // Không cho phép bấm step thủ công khi luồng đang tự động chạy
    }

    @Override
    public void reset(StreamStateContext context) {
        // Phải tạm dừng trước khi reset
        context.doPauseStream();
        context.doResetStream();
        context.setState(new IdleState());
    }

    @Override
    public String getStateName() {
        return "RUNNING";
    }

    @Override
    public String getStatusMessage() {
        return "Đang chạy ●";
    }

    @Override
    public String getStatusColor() {
        return "#16a34a"; // Green
    }

    @Override
    public boolean isStartAllowed() {
        return false;
    }

    @Override
    public boolean isPauseAllowed() {
        return true;
    }

    @Override
    public boolean isStepAllowed() {
        return false;
    }

    @Override
    public boolean isResetAllowed() {
        return false;
    }
}

package vn.edu.dlu.dhopm.pattern.state;

/**
 * Concrete State: Trạng thái Sẵn sàng / Rảnh rỗi (Idle / Ready).
 *
 * <p>Hệ thống đã nạp dữ liệu ban đầu và sẵn sàng nhận lệnh từ người dùng.
 * Cho phép Bắt đầu luồng, Bơm từng bước đơn lẻ, hoặc Khôi phục ban đầu.</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class IdleState implements StreamState {

    @Override
    public void start(StreamStateContext context, long periodMs) {
        if (!context.hasMoreData()) {
            context.setState(new CompletedState());
            return;
        }
        context.setState(new RunningState());
        context.doStartStream(periodMs);
    }

    @Override
    public void pause(StreamStateContext context) {
        // Khong the pause khi dang idle
    }

    @Override
    public void step(StreamStateContext context) {
        if (!context.hasMoreData()) {
            context.setState(new CompletedState());
            return;
        }
        context.doStepStream();
        if (!context.hasMoreData()) {
            context.setState(new CompletedState());
        }
    }

    @Override
    public void reset(StreamStateContext context) {
        context.doResetStream();
    }

    @Override
    public String getStateName() {
        return "IDLE";
    }

    @Override
    public String getStatusMessage() {
        return "Sẵn sàng";
    }

    @Override
    public String getStatusColor() {
        return "#2563eb"; // Blue
    }

    @Override
    public boolean isStartAllowed() {
        return true;
    }

    @Override
    public boolean isPauseAllowed() {
        return false;
    }

    @Override
    public boolean isStepAllowed() {
        return true;
    }

    @Override
    public boolean isResetAllowed() {
        return true;
    }
}

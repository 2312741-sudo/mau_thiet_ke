package vn.edu.dlu.dhopm.pattern.state;

/**
 * Concrete State: Trạng thái Tạm dừng (Paused).
 *
 * <p>Tiến trình tự động đã dừng. Người dùng có thể:
 * - Tiếp tục chạy luồng (Resume / Start)
 * - Hoặc bơm từng bước thủ công (Step Next)
 * - Hoặc khôi phục lại từ đầu (Reset)</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class PausedState implements StreamState {

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
        // Đã ở trạng thái tạm dừng
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
        context.setState(new IdleState());
    }

    @Override
    public String getStateName() {
        return "PAUSED";
    }

    @Override
    public String getStatusMessage() {
        return "Tạm dừng ⏸";
    }

    @Override
    public String getStatusColor() {
        return "#ea580c"; // Orange
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

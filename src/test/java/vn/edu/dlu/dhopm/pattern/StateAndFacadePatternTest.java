package vn.edu.dlu.dhopm.pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.dlu.dhopm.pattern.facade.StreamMiningFacade;
import vn.edu.dlu.dhopm.pattern.state.*;
import vn.edu.dlu.dhopm.pattern.strategy.LandmarkDecay;
import vn.edu.dlu.dhopm.pattern.strategy.NoPruningStrategy;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động kiểm chứng <b>State Pattern &amp; Facade Pattern (GoF)</b>.
 *
 * <p>Kiểm chứng:
 * 1. Chuyển đổi trạng thái máy hữu hạn (Idle -> Running -> Paused -> Completed)
 * 2. Tính năng kiểm soát nút bấm an toàn theo trạng thái
 * 3. Điểm truy cập duy nhất StreamMiningFacade che giấu độ phức tạp cho Client/UI</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class StateAndFacadePatternTest {

    @Test
    @DisplayName("State - State Permissions: Kiểm tra tính khả dụng của các nút theo trạng thái")
    void testStatePermissions() {
        // 1. IdleState
        StreamState idle = new IdleState();
        assertTrue(idle.isStartAllowed());
        assertFalse(idle.isPauseAllowed());
        assertTrue(idle.isStepAllowed());
        assertTrue(idle.isResetAllowed());

        // 2. RunningState
        StreamState running = new RunningState();
        assertFalse(running.isStartAllowed());
        assertTrue(running.isPauseAllowed());
        assertFalse(running.isStepAllowed());
        assertFalse(running.isResetAllowed());

        // 3. PausedState
        StreamState paused = new PausedState();
        assertTrue(paused.isStartAllowed());
        assertFalse(paused.isPauseAllowed());
        assertTrue(paused.isStepAllowed());
        assertTrue(paused.isResetAllowed());

        // 4. CompletedState
        StreamState completed = new CompletedState();
        assertFalse(completed.isStartAllowed());
        assertFalse(completed.isPauseAllowed());
        assertFalse(completed.isStepAllowed());
        assertTrue(completed.isResetAllowed());
    }

    @Test
    @DisplayName("Facade - Unified Entry Point: Khởi tạo, bơm bước, Undo và hoán đổi chiến lược")
    void testStreamMiningFacadeOperations() {
        StreamMiningFacade facade = new StreamMiningFacade();

        // 1. Khởi tạo dữ liệu bài báo mặc định
        facade.initializeDefaultPaperData();
        assertEquals(8, facade.getCurrentTL());
        assertEquals(8, facade.getAllTransactions().size());
        assertTrue(facade.getState() instanceof IdleState);

        // 2. Bơm 1 bước qua Facade (Step Next Command)
        boolean stepped = facade.stepNext();
        assertTrue(stepped);
        assertEquals(9, facade.getCurrentTL());
        assertEquals(9, facade.getAllTransactions().size());
        assertTrue(facade.canUndo());

        // 3. Hoàn tác qua Facade (Undo)
        boolean undone = facade.undo();
        assertTrue(undone);
        assertEquals(8, facade.getCurrentTL());
        assertEquals(8, facade.getAllTransactions().size());

        // 4. Hoán đổi DecayStrategy qua Facade
        facade.setDecayStrategy(new LandmarkDecay());
        assertEquals("Landmark Window (f=1.0)", facade.getCurrentConfig().getDecayStrategy().getName());

        MiningCycleResult result = facade.recalculateAndMine();
        assertEquals(9, result.dhopPatterns().size(), "Landmark Window phải tìm được 9 DHOPs");

        // 5. Hoán đổi PruningStrategy qua Facade
        facade.setPruningStrategy(new NoPruningStrategy());
        assertEquals("No Pruning (Exhaustive DFS)", facade.getCurrentConfig().getPruningStrategy().getName());
        MiningCycleResult noPruneResult = facade.recalculateAndMine();
        assertTrue(noPruneResult.allVisitedPatterns().size() >= result.allVisitedPatterns().size());
    }

    @Test
    @DisplayName("State - Transitions via Facade: Chuyển đổi trạng thái luồng an toàn")
    void testStateTransitionsInFacade() {
        StreamMiningFacade facade = new StreamMiningFacade();
        facade.initializeDefaultPaperData();
        assertTrue(facade.getState() instanceof IdleState);

        // Chuyển sang Running
        facade.startStream();
        assertTrue(facade.getState() instanceof RunningState);

        // Chuyển sang Paused
        facade.pauseStream();
        assertTrue(facade.getState() instanceof PausedState);

        // Tiếp tục chạy rồi dừng
        facade.startStream();
        assertTrue(facade.getState() instanceof RunningState);
        facade.pauseStream();
        assertTrue(facade.getState() instanceof PausedState);
    }

    @Test
    @DisplayName("Facade & Simulator - Live Stream: Tự động bơm giao dịch, kích hoạt callback UI và hoàn thành")
    void testAutomaticStreamingAndCompletion() throws InterruptedException {
        StreamMiningFacade facade = new StreamMiningFacade();
        facade.initializeDefaultPaperData();

        java.util.concurrent.atomic.AtomicInteger uiCallbackCount = new java.util.concurrent.atomic.AtomicInteger(0);
        facade.setOnUIUpdate(uiCallbackCount::incrementAndGet);

        // Nạp 2 giao dịch thử nghiệm với chu kỳ nhanh (60ms)
        List<vn.edu.dlu.dhopm.model.Transaction> testQueue = List.of(
                new vn.edu.dlu.dhopm.model.Transaction(9, List.of("A", "C")),
                new vn.edu.dlu.dhopm.model.Transaction(10, List.of("B", "D"))
        );
        facade.getSimulator().setStreamQueue(testQueue);

        // Khởi động luồng ngầm
        facade.doStartStream(80);
        facade.setState(new RunningState());

        // Chờ luồng xử lý xong (300ms delay + 2 ticks * 80ms ~ 500ms)
        Thread.sleep(700);

        // Kiểm tra kết quả
        assertEquals(10, facade.getCurrentTL());
        assertEquals(10, facade.getAllTransactions().size());
        assertTrue(facade.getState() instanceof CompletedState, "Luồng phải tự động chuyển sang CompletedState khi hết giao dịch");
        assertTrue(uiCallbackCount.get() >= 2, "Callback cập nhật UI phải được gọi ít nhất 2 lần khi giao dịch tới");
    }
}

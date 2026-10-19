package vn.edu.dlu.dhopm.pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.dlu.dhopm.core.DatasetLoader;
import vn.edu.dlu.dhopm.core.DHOPMEngine;
import vn.edu.dlu.dhopm.core.StreamSimulator;
import vn.edu.dlu.dhopm.model.DHONode;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.command.*;
import vn.edu.dlu.dhopm.pattern.memento.DHOListMemento;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động kiểm chứng <b>Command Pattern &amp; Memento Pattern (GoF)</b>.
 *
 * <p>Kiểm chứng:
 * 1. Tính bất biến và khả năng khôi phục sâu của DHOListMemento
 * 2. Tính năng Undo / Step-Back của StepNextCommand
 * 3. Khả năng khôi phục hàng loạt với InjectBatchCommand và ResetCommand
 * 4. Ngăn xếp Undo/Redo trong CommandManager</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class MementoAndCommandPatternTest {

    private DHOPMEngine engine;
    private StreamSimulator simulator;
    private CommandManager commandManager;

    @BeforeEach
    void setUp() {
        engine = new DHOPMEngine();
        simulator = new StreamSimulator(engine);
        commandManager = new CommandManager(20);

        // Khởi đầu với 8 giao dịch gốc
        engine.phase1_constructOrUpdate(DatasetLoader.getPaperDataset());
        simulator.setStreamQueue(DatasetLoader.getSyntheticStreamDataset());
    }

    @Test
    @DisplayName("Memento - Snapshot & Restore: Khôi phục chính xác DHO-List và TL")
    void testDHOListMementoDeepSnapshot() {
        assertEquals(8, engine.getCurrentTL());
        int originalTransCount = engine.getAllTransactions().size();
        assertEquals(8, originalTransCount);

        // 1. Lưu snapshot trạng thái ban đầu
        DHOListMemento snapshot = engine.createMemento(0);
        assertNotNull(snapshot);
        assertEquals(8, snapshot.getSavedTL());

        // 2. Thay đổi trạng thái bằng cách nạp thêm T9
        Transaction t9 = new Transaction(9, Arrays.asList("A", "E"));
        engine.phase1_constructOrUpdate(Collections.singletonList(t9));
        assertEquals(9, engine.getCurrentTL());
        assertEquals(9, engine.getAllTransactions().size());
        assertEquals(5, engine.getGlobalList().getNode("A").getSupport());

        // 3. Khôi phục lại từ snapshot
        engine.restoreFromMemento(snapshot);
        assertEquals(8, engine.getCurrentTL(), "TL phải quay về 8 sau khi restore");
        assertEquals(8, engine.getAllTransactions().size(), "Tổng giao dịch phải quay về 8");
        assertEquals(4, engine.getGlobalList().getNode("A").getSupport(), "Support của A phải quay về 4");
    }

    @Test
    @DisplayName("Command & Memento - StepNextCommand: Thực thi bơm và Undo lùi lại 1 giao dịch")
    void testStepNextCommandExecuteAndUndo() {
        assertEquals(8, engine.getCurrentTL());
        int initialSupportA = engine.getGlobalList().getNode("A").getSupport();

        // 1. Thực thi StepNextCommand (Bơm T9={A, E})
        StepNextCommand stepCmd = new StepNextCommand(engine, simulator, null);
        commandManager.executeCommand(stepCmd);

        assertEquals(9, engine.getCurrentTL(), "Sau khi step, TL phải là 9");
        assertEquals(initialSupportA + 1, engine.getGlobalList().getNode("A").getSupport(), "A phải tăng support");
        assertTrue(commandManager.canUndo(), "CommandManager phải cho phép Undo");

        // 2. Thực hiện Undo (Lùi lại giao dịch trước)
        boolean undoSuccess = commandManager.undo();
        assertTrue(undoSuccess, "Undo phải thành công");
        assertEquals(8, engine.getCurrentTL(), "Sau Undo, TL phải lùi về 8");
        assertEquals(initialSupportA, engine.getGlobalList().getNode("A").getSupport(), "Support của A phải hoàn tác về ban đầu");
        assertEquals(0, simulator.getProcessedCount(), "Chỉ số hàng đợi stream phải lùi về 0");

        // 3. Thực hiện Redo (Làm lại thao tác vừa Undo)
        assertTrue(commandManager.canRedo(), "CommandManager phải cho phép Redo");
        boolean redoSuccess = commandManager.redo();
        assertTrue(redoSuccess, "Redo phải thành công");
        assertEquals(9, engine.getCurrentTL(), "Sau Redo, TL phải trở lại 9");
        assertEquals(initialSupportA + 1, engine.getGlobalList().getNode("A").getSupport());
    }

    @Test
    @DisplayName("Command & Memento - Multi-Step Undo: Bơm 3 bước liên tiếp rồi Undo 3 lần liên tiếp")
    void testMultipleStepNextAndUndos() {
        assertEquals(8, engine.getCurrentTL());

        // Bơm 3 giao dịch: T9, T10, T11
        for (int i = 0; i < 3; i++) {
            StepNextCommand cmd = new StepNextCommand(engine, simulator, null);
            commandManager.executeCommand(cmd);
        }

        assertEquals(11, engine.getCurrentTL());
        assertEquals(11, engine.getAllTransactions().size());
        assertEquals(3, commandManager.getUndoCount());

        // Lùi lại bước 3: về T10
        commandManager.undo();
        assertEquals(10, engine.getCurrentTL());
        assertEquals(10, engine.getAllTransactions().size());

        // Lùi lại bước 2: về T9
        commandManager.undo();
        assertEquals(9, engine.getCurrentTL());
        assertEquals(9, engine.getAllTransactions().size());

        // Lùi lại bước 1: về T8 ban đầu
        commandManager.undo();
        assertEquals(8, engine.getCurrentTL());
        assertEquals(8, engine.getAllTransactions().size());
        assertFalse(commandManager.canUndo(), "Không còn bước nào để Undo");
    }

    @Test
    @DisplayName("Command - InjectBatchCommand: Nạp lô giao dịch và hoàn tác")
    void testInjectBatchCommand() {
        List<Transaction> customBatch = Arrays.asList(
                new Transaction(100, Arrays.asList("X", "Y")),
                new Transaction(101, Arrays.asList("X", "Z"))
        );

        InjectBatchCommand injectCmd = new InjectBatchCommand(engine, simulator, customBatch, null);
        commandManager.executeCommand(injectCmd);

        assertEquals(101, engine.getCurrentTL());
        assertEquals(10, engine.getAllTransactions().size());
        assertNotNull(engine.getGlobalList().getNode("X"));

        // Undo
        commandManager.undo();
        assertEquals(8, engine.getCurrentTL());
        assertEquals(8, engine.getAllTransactions().size());
        assertNull(engine.getGlobalList().getNode("X"), "Item X không được tồn tại sau khi Undo");
    }

    @Test
    @DisplayName("Command - ResetCommand: Khôi phục hệ thống và Undo khôi phục nếu bấm nhầm")
    void testResetCommand() {
        // Bơm thêm T9
        commandManager.executeCommand(new StepNextCommand(engine, simulator, null));
        assertEquals(9, engine.getCurrentTL());

        // Reset về DB0 (4 giao dịch)
        ResetCommand resetCmd = new ResetCommand(
                engine,
                simulator,
                DatasetLoader.getDB0Dataset(),
                DatasetLoader.getSyntheticStreamDataset(),
                null
        );
        commandManager.executeCommand(resetCmd);
        assertEquals(4, engine.getCurrentTL());
        assertEquals(4, engine.getAllTransactions().size());

        // Undo lệnh Reset -> Trở lại trạng thái trước khi Reset (TL=9)
        commandManager.undo();
        assertEquals(9, engine.getCurrentTL(), "Undo Reset phải đưa hệ thống về TL=9");
        assertEquals(9, engine.getAllTransactions().size());
    }
}

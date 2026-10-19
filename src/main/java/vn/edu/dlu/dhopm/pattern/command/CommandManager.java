package vn.edu.dlu.dhopm.pattern.command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Invoker và Caretaker trong <b>Command Pattern &amp; Memento Pattern (GoF)</b>.
 *
 * <p>Quản lý ngăn xếp lịch sử thực thi (Undo Stack và Redo Stack), điều phối
 * việc thực thi lệnh và hoàn tác giao dịch trên luồng dữ liệu.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class CommandManager {

    private final Deque<StreamCommand> undoStack = new ArrayDeque<>();
    private final Deque<StreamCommand> redoStack = new ArrayDeque<>();
    private final List<String> historyLog = new ArrayList<>();
    private final int maxHistorySize;

    public CommandManager() {
        this(50);
    }

    public CommandManager(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * Thực thi một lệnh mới và lưu vào ngăn xếp Undo.
     */
    public synchronized void executeCommand(StreamCommand command) {
        if (command == null) return;

        command.execute();

        if (command.canUndo()) {
            if (undoStack.size() >= maxHistorySize) {
                undoStack.removeLast();
            }
            undoStack.push(command);
            redoStack.clear();
        }

        historyLog.add("EXECUTE: " + command.getDescription());
    }

    /**
     * Hoàn tác lệnh gần nhất (Undo / Step-Back).
     *
     * @return true nếu hoàn tác thành công; false nếu không còn lệnh để hoàn tác
     */
    public synchronized boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }

        StreamCommand cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        historyLog.add("UNDO: " + cmd.getDescription());
        return true;
    }

    /**
     * Làm lại lệnh vừa bị hoàn tác (Redo).
     *
     * @return true nếu làm lại thành công; false nếu không có lệnh để redo
     */
    public synchronized boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }

        StreamCommand cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        historyLog.add("REDO: " + cmd.getDescription());
        return true;
    }

    public synchronized boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public synchronized boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public synchronized int getUndoCount() {
        return undoStack.size();
    }

    public synchronized int getRedoCount() {
        return redoStack.size();
    }

    public synchronized void clear() {
        undoStack.clear();
        redoStack.clear();
        historyLog.clear();
    }

    public synchronized List<String> getHistoryLog() {
        return Collections.unmodifiableList(historyLog);
    }

    public synchronized List<String> getUndoStackDescriptions() {
        List<String> list = new ArrayList<>();
        int step = undoStack.size();
        for (StreamCommand cmd : undoStack) {
            list.add("Snapshot #" + (step--) + ": " + cmd.getDescription());
        }
        return list;
    }
}

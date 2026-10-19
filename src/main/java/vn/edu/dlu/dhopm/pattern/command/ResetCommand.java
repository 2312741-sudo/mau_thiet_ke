package vn.edu.dlu.dhopm.pattern.command;

import vn.edu.dlu.dhopm.core.DHOPMEngine;
import vn.edu.dlu.dhopm.core.StreamSimulator;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.memento.DHOListMemento;

import java.util.List;
import java.util.Objects;

/**
 * Concrete Command thực hiện đặt lại (Reset) hệ thống về tập dữ liệu khởi tạo.
 *
 * <p>Vẫn hỗ trợ hoàn tác Undo nếu người dùng bấm nhầm nút Reset.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class ResetCommand implements StreamCommand {

    private final DHOPMEngine engine;
    private final StreamSimulator simulator;
    private final List<Transaction> initialTransactions;
    private final List<Transaction> newPendingQueue;
    private final Runnable postActionCallback;
    private DHOListMemento snapshot;

    public ResetCommand(
            DHOPMEngine engine,
            StreamSimulator simulator,
            List<Transaction> initialTransactions,
            List<Transaction> newPendingQueue,
            Runnable postActionCallback
    ) {
        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.simulator = Objects.requireNonNull(simulator, "simulator must not be null");
        this.initialTransactions = initialTransactions != null ? List.copyOf(initialTransactions) : List.of();
        this.newPendingQueue = newPendingQueue != null ? List.copyOf(newPendingQueue) : List.of();
        this.postActionCallback = postActionCallback;
    }

    @Override
    public void execute() {
        this.snapshot = engine.createMemento(simulator.getProcessedCount());

        simulator.stop();
        engine.reset();
        engine.phase1_constructOrUpdate(initialTransactions);
        simulator.setStreamQueue(newPendingQueue);

        if (postActionCallback != null) {
            postActionCallback.run();
        }
    }

    @Override
    public void undo() {
        if (snapshot != null) {
            engine.restoreFromMemento(snapshot);
            simulator.setProcessedCount(snapshot.getSavedStreamNextIndex());

            if (postActionCallback != null) {
                postActionCallback.run();
            }
        }
    }

    @Override
    public String getDescription() {
        return "Khôi phục hệ thống về trạng thái ban đầu (" + initialTransactions.size() + " giao dịch)";
    }
}

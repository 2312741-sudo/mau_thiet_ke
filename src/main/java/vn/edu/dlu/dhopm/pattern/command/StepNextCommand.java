package vn.edu.dlu.dhopm.pattern.command;

import vn.edu.dlu.dhopm.core.DHOPMEngine;
import vn.edu.dlu.dhopm.core.StreamSimulator;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.memento.DHOListMemento;

import java.util.Objects;

/**
 * Concrete Command thực hiện bơm một giao dịch tiếp theo vào luồng.
 *
 * <p>Kết hợp <b>Memento Pattern</b> để lưu trạng thái trước khi thực thi
 * và hỗ trợ tính năng <b>Undo / Step-Back (Lùi lại giao dịch trước)</b>.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class StepNextCommand implements StreamCommand {

    private final DHOPMEngine engine;
    private final StreamSimulator simulator;
    private final Runnable postActionCallback;
    private DHOListMemento snapshot;
    private Transaction executedTransaction;

    public StepNextCommand(DHOPMEngine engine, StreamSimulator simulator, Runnable postActionCallback) {
        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.simulator = Objects.requireNonNull(simulator, "simulator must not be null");
        this.postActionCallback = postActionCallback;
    }

    @Override
    public void execute() {
        // Lưu ảnh chụp nhanh trước khi bơm
        this.snapshot = engine.createMemento(simulator.getProcessedCount());
        this.executedTransaction = simulator.stepNext();

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
        if (executedTransaction != null) {
            return "Bơm giao dịch T" + executedTransaction.getTid() + " " + executedTransaction.getItems();
        }
        return "Bơm 1 giao dịch tiếp theo";
    }

    public Transaction getExecutedTransaction() {
        return executedTransaction;
    }
}

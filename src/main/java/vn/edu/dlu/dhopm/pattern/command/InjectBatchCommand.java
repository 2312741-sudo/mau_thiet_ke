package vn.edu.dlu.dhopm.pattern.command;

import vn.edu.dlu.dhopm.core.DHOPMEngine;
import vn.edu.dlu.dhopm.core.StreamSimulator;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.memento.DHOListMemento;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Concrete Command thực hiện bơm một lô (batch) giao dịch tùy biến vào luồng.
 *
 * <p>Kết hợp <b>Memento Pattern</b> để hỗ trợ hoàn tác toàn bộ lô giao dịch.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class InjectBatchCommand implements StreamCommand {

    private final DHOPMEngine engine;
    private final StreamSimulator simulator;
    private final List<Transaction> batch;
    private final Runnable postActionCallback;
    private DHOListMemento snapshot;

    public InjectBatchCommand(
            DHOPMEngine engine,
            StreamSimulator simulator,
            List<Transaction> batch,
            Runnable postActionCallback
    ) {
        this.engine = Objects.requireNonNull(engine, "engine must not be null");
        this.simulator = Objects.requireNonNull(simulator, "simulator must not be null");
        this.batch = batch != null ? List.copyOf(batch) : Collections.emptyList();
        this.postActionCallback = postActionCallback;
    }

    @Override
    public void execute() {
        this.snapshot = engine.createMemento(simulator.getProcessedCount());
        engine.phase1_constructOrUpdate(batch);

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
        return "Nạp lô gồm " + batch.size() + " giao dịch vào luồng";
    }

    public List<Transaction> getBatch() {
        return batch;
    }
}

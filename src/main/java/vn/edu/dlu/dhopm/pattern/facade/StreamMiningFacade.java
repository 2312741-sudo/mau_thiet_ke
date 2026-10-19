package vn.edu.dlu.dhopm.pattern.facade;

import vn.edu.dlu.dhopm.core.DHOPMEngine;
import vn.edu.dlu.dhopm.core.DatasetLoader;
import vn.edu.dlu.dhopm.core.StreamSimulator;
import vn.edu.dlu.dhopm.model.DHOList;
import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.command.CommandManager;
import vn.edu.dlu.dhopm.pattern.command.InjectBatchCommand;
import vn.edu.dlu.dhopm.pattern.command.ResetCommand;
import vn.edu.dlu.dhopm.pattern.command.StepNextCommand;
import vn.edu.dlu.dhopm.pattern.decorator.BenchmarkMinerDecorator;
import vn.edu.dlu.dhopm.pattern.decorator.LoggingMinerDecorator;
import vn.edu.dlu.dhopm.pattern.decorator.StreamMiner;
import vn.edu.dlu.dhopm.pattern.observer.AuditLogObserver;
import vn.edu.dlu.dhopm.pattern.observer.BenchmarkMetricsObserver;
import vn.edu.dlu.dhopm.pattern.observer.MiningObserver;
import vn.edu.dlu.dhopm.pattern.observer.StreamObserver;
import vn.edu.dlu.dhopm.pattern.state.*;
import vn.edu.dlu.dhopm.pattern.strategy.DampedExponentialDecay;
import vn.edu.dlu.dhopm.pattern.strategy.DUBOPruningStrategy;
import vn.edu.dlu.dhopm.pattern.strategy.DecayStrategy;
import vn.edu.dlu.dhopm.pattern.strategy.PruningStrategy;
import vn.edu.dlu.dhopm.pattern.template.DefaultStreamMiner;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Facade Class trong <b>Facade Pattern (GoF)</b>.
 *
 * <p>Đóng vai trò là điểm truy cập duy nhất (Unified High-Level Interface) cho toàn bộ
 * hệ thống khai phá luồng dữ liệu DHOPM. Che giấu độ phức tạp về:
 * <ul>
 *   <li>Quản lý đa luồng (Multithreading / ScheduledExecutorService)</li>
 *   <li>Điều phối trạng thái máy hữu hạn (State Pattern)</li>
 *   <li>Ngăn xếp hoàn tác/làm lại (Command &amp; Memento Pattern)</li>
 *   <li>Bọc chuỗi các decorator đo lường và ghi log (Decorator Pattern)</li>
 *   <li>Phát thông báo cho danh sách các bên quan sát (Observer Pattern)</li>
 * </ul>
 * </p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class StreamMiningFacade implements StreamStateContext {

    // Core Engine & Simulator
    private final DHOPMEngine engine;
    private final StreamSimulator simulator;

    // StreamMiner decorated pipeline
    private final DefaultStreamMiner baseMiner;
    private final BenchmarkMinerDecorator benchmarkDecorator;
    private final LoggingMinerDecorator loggingDecorator;

    // Pattern components
    private final CommandManager commandManager;
    private StreamState currentState;
    private MiningConfig currentConfig;

    // Observers
    private final List<StreamObserver> streamObservers = new CopyOnWriteArrayList<>();
    private final List<MiningObserver> miningObservers = new CopyOnWriteArrayList<>();
    private final AuditLogObserver auditLogObserver;
    private final BenchmarkMetricsObserver benchmarkMetricsObserver;

    // UI Hook Callback
    private Runnable uiUpdateCallback;

    public StreamMiningFacade() {
        this.engine = new DHOPMEngine();
        this.simulator = new StreamSimulator(this.engine);
        this.commandManager = new CommandManager(50);
        this.currentState = new IdleState();

        // Default Config
        this.currentConfig = MiningConfig.defaultPaperConfig();

        // Xây dựng pipeline Decorator: Base -> Benchmark -> Logging
        this.baseMiner = new DefaultStreamMiner(engine.getGlobalList(), engine.getRawTransactions(), 0);
        this.benchmarkDecorator = new BenchmarkMinerDecorator(baseMiner);
        this.loggingDecorator = new LoggingMinerDecorator(benchmarkDecorator);

        // Observers mặc định
        this.auditLogObserver = new AuditLogObserver();
        this.benchmarkMetricsObserver = new BenchmarkMetricsObserver();
        addMiningObserver(auditLogObserver);
        addMiningObserver(benchmarkMetricsObserver);
        addStreamObserver(auditLogObserver);

        // Lắng nghe sự kiện từ engine để chuyển tiếp cho các StreamObserver
        this.engine.addStreamListener((newBatch, currentTL) -> {
            for (StreamObserver observer : streamObservers) {
                try {
                    observer.onBatchArrived(newBatch, currentTL);
                } catch (Exception e) {
                    System.err.println("Lỗi StreamObserver: " + e.getMessage());
                }
            }
        });

        // Kết nối bộ mô phỏng luồng (StreamSimulator) với UI và State Machine
        this.simulator.setOnTransactionProcessed(tx -> {
            notifyUI();
        });
        this.simulator.setOnStreamCompleted(() -> {
            setState(new CompletedState());
        });
    }

    /**
     * Khởi tạo tập dữ liệu ban đầu từ bài báo gốc.
     */
    public void initializeDefaultPaperData() {
        simulator.stop();
        engine.reset();

        List<Transaction> paperData = DatasetLoader.getPaperDataset();
        engine.phase1_constructOrUpdate(paperData);

        simulator.setStreamQueue(DatasetLoader.getSyntheticStreamDataset());
        commandManager.clear();
        setState(new IdleState());

        recalculateAndMine();
    }

    // =========================================================================
    // DIEU KHIEN STREAM & COMMAND PATTERN
    // =========================================================================

    public synchronized void startStream() {
        currentState.start(this, currentConfig.getStreamBatchPeriodMs());
    }

    public synchronized void pauseStream() {
        currentState.pause(this);
    }

    public synchronized boolean stepNext() {
        if (!hasMoreData()) {
            setState(new CompletedState());
            return false;
        }

        StepNextCommand cmd = new StepNextCommand(engine, simulator, this::notifyUI);
        commandManager.executeCommand(cmd);

        if (!hasMoreData()) {
            setState(new CompletedState());
        }
        return true;
    }

    public synchronized boolean injectBatch(List<Transaction> batch) {
        if (batch == null || batch.isEmpty()) return false;
        InjectBatchCommand cmd = new InjectBatchCommand(engine, simulator, batch, this::notifyUI);
        commandManager.executeCommand(cmd);
        return true;
    }

    public synchronized void resetToInitial(List<Transaction> initialTransactions, List<Transaction> pendingQueue) {
        ResetCommand cmd = new ResetCommand(engine, simulator, initialTransactions, pendingQueue, this::notifyUI);
        commandManager.executeCommand(cmd);
        setState(new IdleState());
    }

    /**
     * Tính năng Undo / Step-Back (Lùi lại giao dịch trước).
     */
    public synchronized boolean undo() {
        boolean success = commandManager.undo();
        if (success) {
            if (hasMoreData() && currentState instanceof CompletedState) {
                setState(new PausedState());
            }
            notifyUI();
        }
        return success;
    }

    /**
     * Tính năng Redo (Làm lại thao tác vừa hoàn tác).
     */
    public synchronized boolean redo() {
        boolean success = commandManager.redo();
        if (success) {
            notifyUI();
        }
        return success;
    }

    public boolean canUndo() {
        return commandManager.canUndo();
    }

    public boolean canRedo() {
        return commandManager.canRedo();
    }

    // =========================================================================
    // STATE PATTERN CALLBACKS (StreamStateContext)
    // =========================================================================

    @Override
    public void setState(StreamState newState) {
        this.currentState = newState;
        notifyUI();
    }

    @Override
    public StreamState getState() {
        return currentState;
    }

    @Override
    public void doStartStream(long periodMs) {
        simulator.start(periodMs);
    }

    @Override
    public void doPauseStream() {
        simulator.stop();
    }

    @Override
    public void doStepStream() {
        Transaction t = simulator.stepNext();
        if (t != null) {
            notifyUI();
        }
    }

    @Override
    public void doResetStream() {
        initializeDefaultPaperData();
    }

    @Override
    public boolean hasMoreData() {
        return simulator.hasMore();
    }

    // =========================================================================
    // KHAI PHA & HOAN DOI CHIEN LUOC (STRATEGY & DECORATOR)
    // =========================================================================

    public synchronized MiningCycleResult recalculateAndMine() {
        int TL = engine.getCurrentTL();
        double minSup = currentConfig.resolveAbsoluteMinSup(engine.getAllTransactions().size());

        // 1. Tái cấu trúc danh sách DHO-List theo DecayStrategy
        engine.phase2_reconstruct(currentConfig.getDecayStrategy(), TL);
        baseMiner.setCurrentTL(TL);

        // 2. Khai phá thông qua Decorator Pipeline (Logging -> Benchmark -> Base)
        List<PatternResult> results = loggingDecorator.mine(currentConfig);

        long elapsedNs = benchmarkDecorator.getLastExecutionTimeNs();
        long memUsed = benchmarkDecorator.getLastMemoryUsedBytes();

        List<PatternResult> dhops = results.stream().filter(PatternResult::isDHOP).toList();
        MiningCycleResult cycleResult = new MiningCycleResult(
                Collections.emptyList(),
                TL,
                results,
                dhops,
                elapsedNs,
                memUsed
        );

        // Báo cho các MiningObserver
        for (MiningObserver observer : miningObservers) {
            try {
                observer.onMiningCompleted(cycleResult);
            } catch (Exception e) {
                System.err.println("Lỗi MiningObserver: " + e.getMessage());
            }
        }

        return cycleResult;
    }

    public synchronized void setDecayStrategy(DecayStrategy strategy) {
        if (strategy == null) return;
        this.currentConfig = MiningConfig.builder()
                .decayStrategy(strategy)
                .minSupRatio(currentConfig.getMinSupRatio())
                .pruningStrategy(currentConfig.getPruningStrategy())
                .streamBatchPeriodMs(currentConfig.getStreamBatchPeriodMs())
                .build();
        recalculateAndMine();
        notifyUI();
    }

    public synchronized void setPruningStrategy(PruningStrategy strategy) {
        if (strategy == null) return;
        this.currentConfig = MiningConfig.builder()
                .decayStrategy(currentConfig.getDecayStrategy())
                .minSupRatio(currentConfig.getMinSupRatio())
                .pruningStrategy(strategy)
                .streamBatchPeriodMs(currentConfig.getStreamBatchPeriodMs())
                .build();
        recalculateAndMine();
        notifyUI();
    }

    public synchronized void setDecayFactor(double f) {
        setDecayStrategy(new DampedExponentialDecay(f));
    }

    public synchronized void setMinSupRatio(double ratio) {
        this.currentConfig = MiningConfig.builder()
                .decayStrategy(currentConfig.getDecayStrategy())
                .minSupRatio(ratio)
                .pruningStrategy(currentConfig.getPruningStrategy())
                .streamBatchPeriodMs(currentConfig.getStreamBatchPeriodMs())
                .build();
        recalculateAndMine();
        notifyUI();
    }

    // =========================================================================
    // OBSERVER MANAGEMENT
    // =========================================================================

    public void addStreamObserver(StreamObserver observer) {
        if (observer != null) streamObservers.add(observer);
    }

    public void addMiningObserver(MiningObserver observer) {
        if (observer != null) miningObservers.add(observer);
    }

    public void setOnUIUpdate(Runnable callback) {
        this.uiUpdateCallback = callback;
    }

    private void notifyUI() {
        if (uiUpdateCallback != null) {
            uiUpdateCallback.run();
        }
    }

    // =========================================================================
    // GETTERS CHO TANG GIAO DIEN & KIEM THU
    // =========================================================================

    public DHOPMEngine getEngine() {
        return engine;
    }

    public StreamSimulator getSimulator() {
        return simulator;
    }

    public DHOList getGlobalList() {
        return engine.getGlobalList();
    }

    public List<Transaction> getAllTransactions() {
        return engine.getAllTransactions();
    }

    public int getCurrentTL() {
        return engine.getCurrentTL();
    }

    public MiningConfig getCurrentConfig() {
        return currentConfig;
    }

    public BenchmarkMinerDecorator getBenchmarkDecorator() {
        return benchmarkDecorator;
    }

    public BenchmarkMetricsObserver getBenchmarkMetricsObserver() {
        return benchmarkMetricsObserver;
    }

    public AuditLogObserver getAuditLogObserver() {
        return auditLogObserver;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public synchronized void loadDatasetFromFactory(vn.edu.dlu.dhopm.pattern.factory.DatasetLoaderFactory factory) {
        if (factory == null) return;
        List<Transaction> dataset = factory.createDataset();
        if (dataset == null || dataset.isEmpty()) return;

        simulator.stop();
        engine.reset();
        engine.phase1_constructOrUpdate(dataset);

        List<Transaction> streamQueue = new java.util.ArrayList<>();
        if (factory instanceof vn.edu.dlu.dhopm.pattern.factory.DB0DatasetFactory) {
            List<Transaction> paper = DatasetLoader.getPaperDataset();
            streamQueue.addAll(paper.subList(4, paper.size()));
            streamQueue.addAll(DatasetLoader.getSyntheticStreamDataset());
        } else {
            streamQueue.addAll(DatasetLoader.getSyntheticStreamDataset());
        }
        simulator.setStreamQueue(streamQueue);

        commandManager.clear();
        setState(new IdleState());
        recalculateAndMine();
        notifyUI();
    }

    public List<String> getUndoStackDescriptions() {
        return commandManager.getUndoStackDescriptions();
    }

    public int getUndoCount() {
        return commandManager.getUndoCount();
    }

    public int getRedoCount() {
        return commandManager.getRedoCount();
    }
}

package vn.edu.dlu.dhopm.core;

import vn.edu.dlu.dhopm.model.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Bo gia lap luong du lieu giao dich thoi gian thuc (Data Stream Simulator).
 * Su dung da luong (Multithreading / ScheduledExecutorService) de bom giao dich vao DHOPMEngine
 * ma khong lam dong bang (freeze) giao dien nguoi dung.
 */
public class StreamSimulator {
    private final DHOPMEngine engine;
    private final List<Transaction> pendingStream;
    private int nextIndex;

    private ScheduledExecutorService scheduler;
    private final AtomicBoolean isRunning;

    private Consumer<Transaction> onTransactionProcessed;
    private Runnable onStreamCompleted;

    public StreamSimulator(DHOPMEngine engine) {
        this.engine = engine;
        this.pendingStream = new ArrayList<>();
        this.nextIndex = 0;
        this.isRunning = new AtomicBoolean(false);
    }

    public void setOnTransactionProcessed(Consumer<Transaction> callback) {
        this.onTransactionProcessed = callback;
    }

    public void setOnStreamCompleted(Runnable callback) {
        this.onStreamCompleted = callback;
    }

    /**
     * Nap danh sach cac giao dich se duoc bom dan qua stream.
     */
    public synchronized void setStreamQueue(List<Transaction> transactions) {
        this.pendingStream.clear();
        this.pendingStream.addAll(transactions);
        this.nextIndex = 0;
    }

    /**
     * Bat dau bom giao dich tu dong theo chu ky (ms).
     */
    public synchronized void start(long periodMs) {
        if (isRunning.get()) return;

        isRunning.set(true);
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DHOPM-Stream-Thread");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                stepNext();
            } catch (Throwable t) {
                System.err.println("Lỗi trong StreamSimulator tick: " + t.getMessage());
                t.printStackTrace();
            }
        }, 300, periodMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Tam dung bom stream.
     */
    public synchronized void stop() {
        if (!isRunning.get()) return;

        isRunning.set(false);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Bom dung 1 giao dich tiep theo trong hang doi (hoac tu dong sinh neu het).
     *
     * @return Giao dich vua duoc bom, hoac null neu khong con
     */
    public synchronized Transaction stepNext() {
        if (nextIndex < pendingStream.size()) {
            Transaction t = pendingStream.get(nextIndex++);
            engine.phase1_constructOrUpdate(Collections.singletonList(t));
            if (onTransactionProcessed != null) {
                try {
                    onTransactionProcessed.accept(t);
                } catch (Exception e) {
                    System.err.println("Lỗi callback onTransactionProcessed: " + e.getMessage());
                }
            }
            return t;
        } else {
            // Khi het du lieu dat san, tu dong dung lai
            stop();
            if (onStreamCompleted != null) {
                try {
                    onStreamCompleted.run();
                } catch (Exception e) {
                    System.err.println("Lỗi callback onStreamCompleted: " + e.getMessage());
                }
            }
            return null;
        }
    }

    public boolean hasMore() {
        return nextIndex < pendingStream.size();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public int getProcessedCount() {
        return nextIndex;
    }

    public synchronized void setProcessedCount(int count) {
        this.nextIndex = Math.max(0, Math.min(count, pendingStream.size()));
    }

    public int getTotalCount() {
        return pendingStream.size();
    }

    public synchronized void reset() {
        stop();
        nextIndex = 0;
    }
}

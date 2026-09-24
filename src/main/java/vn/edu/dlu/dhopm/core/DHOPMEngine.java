package vn.edu.dlu.dhopm.core;

import vn.edu.dlu.dhopm.event.MiningListener;
import vn.edu.dlu.dhopm.event.StreamListener;
import vn.edu.dlu.dhopm.model.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bo may thuc thi thuat toan DHOPM (Cho et al., EAAI 2026).
 *
 * Gom 3 pha:
 * - Pha 1: constructOrUpdate(batch) - Quet 1 lan (One-scan) de cap nhat Global DHO-List
 * - Pha 2: reconstruct(f, TL) - Tinh lai DO va sap xep theo Support tang dan
 * - Pha 3: mine(minSup, f, TL) - Khai pha theo chieu sau (DFS) voi can tren DUBO
 */
public class DHOPMEngine {
    private final DHOList globalList;
    private final List<Transaction> allTransactions;
    private final List<StreamListener> streamListeners;
    private final List<MiningListener> miningListeners;

    private int currentTL;
    private double currentF;
    private double currentMinSup;

    public DHOPMEngine() {
        this.globalList = new DHOList();
        this.allTransactions = new ArrayList<>();
        this.streamListeners = new CopyOnWriteArrayList<>();
        this.miningListeners = new CopyOnWriteArrayList<>();
        this.currentTL = 0;
        this.currentF = 0.9;
        this.currentMinSup = 1.2;
    }

    public void addStreamListener(StreamListener listener) {
        streamListeners.add(listener);
    }

    public void addMiningListener(MiningListener listener) {
        miningListeners.add(listener);
    }

    /**
     * Pha 1: Nhap mot batch giao dich moi vao he thong (Data Stream).
     * Chi quet cac giao dich moi ma khong quet lai cac giao dich cu da xu ly.
     */
    public synchronized void phase1_constructOrUpdate(List<Transaction> newBatch) {
        if (newBatch == null || newBatch.isEmpty()) return;

        allTransactions.addAll(newBatch);
        for (Transaction t : newBatch) {
            if (t.getTid() > currentTL) {
                currentTL = t.getTid();
            }
        }

        globalList.constructOrUpdate(newBatch);

        // Báo cho các observer
        for (StreamListener listener : streamListeners) {
            listener.onBatchArrived(newBatch, currentTL);
        }
    }

    /**
     * Pha 2: Tai cau truc Global DHO-List theo TL va he so f moi nhat.
     */
    public synchronized void phase2_reconstruct(double f, int TL) {
        validateDecayParameters(f);
        this.currentF = f;
        this.currentTL = TL;
        globalList.reconstruct(f, TL);
    }

    /**
     * Pha 3: Khai pha toan bo tap DHOP theo giai thuat DFS Pattern Growth.
     *
     * @param minSup Nguong ho tro toi thieu
     * @param f      He so suy giam
     * @param TL     TID moi nhat
     * @return Danh sach tat ca cac mau ung vien da duyet va ket qua tuong ung
     */
    public synchronized List<PatternResult> phase3_mine(double minSup, double f, int TL) {
        long startTime = System.nanoTime();
        this.currentMinSup = minSup;
        this.currentF = f;
        this.currentTL = TL;

        // Dam bao Global List da duoc reconstruct
        phase2_reconstruct(f, TL);

        List<PatternResult> allVisited = new ArrayList<>();
        List<PatternResult> dhops = new ArrayList<>();

        List<DHONode> sortedNodes = globalList.getSortedNodes();
        List<String> processingOrder = new ArrayList<>();
        for (DHONode node : sortedNodes) {
            processingOrder.add(node.getItemName());
        }

        // Goi de quy DFS
        dfsMining(new ArrayList<>(), new ArrayList<>(), 0, processingOrder, allVisited, dhops, f, TL, minSup);

        long runtimeNs = System.nanoTime() - startTime;

        // Báo cho các MiningListener
        for (MiningListener l : miningListeners) {
            l.onMiningComplete(allVisited, dhops, runtimeNs);
        }

        return allVisited;
    }

    /**
     * De quy DFS pattern expansion.
     */
    private void dfsMining(
            List<String> prefixItems,
            List<Entry> prefixEntries,
            int startIdx,
            List<String> processingOrder,
            List<PatternResult> allVisited,
            List<PatternResult> dhops,
            double f,
            int TL,
            double minSup
    ) {
        for (int i = startIdx; i < processingOrder.size(); i++) {
            String item = processingOrder.get(i);
            DHONode itemNode = globalList.getNode(item);
            if (itemNode == null) continue;

            List<String> candPattern = new ArrayList<>(prefixItems);
            candPattern.add(item);
            String patternName = String.join("", candPattern);

            // Giao cac entry: neu la 1-itemset thi lay luon entry cua itemNode
            List<Entry> candEntries;
            if (prefixItems.isEmpty()) {
                candEntries = itemNode.getEntries();
            } else {
                candEntries = intersectEntries(prefixEntries, itemNode.getEntries());
            }

            if (candEntries.isEmpty()) {
                continue;
            }

            // 1. Tinh DO cua mau ung vien
            double doVal = calculateDO(candEntries, candPattern.size(), f, TL);

            // 2. Tinh DUBO de kiem tra tinh cat tia
            double duboVal = DUBOCalculator.calculate(candEntries, f, TL);

            boolean isDHOP = (doVal >= minSup - 1e-9);
            boolean isPruned = (duboVal < minSup - 1e-9);

            List<Integer> tids = new ArrayList<>();
            for (Entry e : candEntries) {
                tids.add(e.tid());
            }

            PatternResult result = new PatternResult(
                    patternName, doVal, duboVal, candEntries.size(), isDHOP, isPruned, tids
            );
            allVisited.add(result);

            if (isDHOP) {
                dhops.add(result);
            }

            // Neu KHONG bi cat tia boi DUBO, tiep tuc mo rong bang de quy DFS
            if (!isPruned) {
                dfsMining(candPattern, candEntries, i + 1, processingOrder, allVisited, dhops, f, TL, minSup);
            }
        }
    }

    /**
     * Tinh Damped Occupancy cua mot mau:
     * DO(X) = sum ( (|X| / tlen) * f^(TL - tid) )
     */
    public static double calculateDO(List<Entry> entries, int patternLength, double f, int TL) {
        validateDecayParameters(f);
        if (patternLength <= 0) {
            throw new IllegalArgumentException("Pattern length must be positive");
        }

        double sum = 0.0;
        for (Entry e : entries) {
            double occ = (double) patternLength / e.tlen();
            double decay = Math.pow(f, TL - e.tid());
            sum += occ * decay;
        }
        return sum;
    }

    private static void validateDecayParameters(double f) {
        if (!Double.isFinite(f) || f <= 0.0 || f > 1.0) {
            throw new IllegalArgumentException("Decay factor f must be in (0, 1]");
        }
    }

    /**
     * Giao 2 danh sach entries dua tren cung TID.
     */
    public static List<Entry> intersectEntries(List<Entry> listA, List<Entry> listB) {
        List<Entry> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < listA.size() && j < listB.size()) {
            Entry ea = listA.get(i);
            Entry eb = listB.get(j);
            if (ea.tid() == eb.tid()) {
                result.add(ea);
                i++;
                j++;
            } else if (ea.tid() < eb.tid()) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }

    public DHOList getGlobalList() {
        return globalList;
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(allTransactions);
    }

    public List<Transaction> getRawTransactions() {
        return allTransactions;
    }

    public int getCurrentTL() {
        return currentTL;
    }

    public double getCurrentF() {
        return currentF;
    }

    public double getCurrentMinSup() {
        return currentMinSup;
    }

    public void reset() {
        globalList.clear();
        allTransactions.clear();
        currentTL = 0;
    }

    /**
     * Memento Pattern: Tao anh chup nhanh trang thai hien tai cua Engine.
     */
    public synchronized vn.edu.dlu.dhopm.pattern.memento.DHOListMemento createMemento(int streamNextIndex) {
        return globalList.saveToMemento(currentTL, allTransactions, streamNextIndex);
    }

    /**
     * Memento Pattern: Khoi phuc trang thai Engine tu anh chup nhanh.
     */
    public synchronized void restoreFromMemento(vn.edu.dlu.dhopm.pattern.memento.DHOListMemento memento) {
        if (memento == null) return;
        this.globalList.restoreFromMemento(memento);
        this.allTransactions.clear();
        this.allTransactions.addAll(memento.getSavedTransactions());
        this.currentTL = memento.getSavedTL();
    }

    /**
     * Strategy Pattern: Tai cau truc Global DHO-List theo DecayStrategy.
     */
    public synchronized void phase2_reconstruct(vn.edu.dlu.dhopm.pattern.strategy.DecayStrategy strategy, int TL) {
        if (strategy == null) {
            throw new IllegalArgumentException("DecayStrategy must not be null");
        }
        this.currentF = strategy.getParameter();
        this.currentTL = TL;
        globalList.reconstruct(strategy, TL);
    }
}

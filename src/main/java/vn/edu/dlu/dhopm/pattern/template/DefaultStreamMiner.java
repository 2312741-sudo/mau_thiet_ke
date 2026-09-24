package vn.edu.dlu.dhopm.pattern.template;

import vn.edu.dlu.dhopm.core.DUBOCalculator;
import vn.edu.dlu.dhopm.model.DHOList;
import vn.edu.dlu.dhopm.model.DHONode;
import vn.edu.dlu.dhopm.model.Entry;
import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.model.Transaction;
import vn.edu.dlu.dhopm.pattern.builder.MiningConfig;
import vn.edu.dlu.dhopm.pattern.strategy.DecayStrategy;
import vn.edu.dlu.dhopm.pattern.strategy.PruningStrategy;

import java.util.*;

/**
 * Concrete Class triển khai giải thuật khai phá luồng chuẩn DHOPM
 * theo <b>Template Method Pattern (GoF)</b>.
 *
 * <p>Hiện thực đầy đủ 5 primitive operations của {@link AbstractStreamMiner},
 * tích hợp linh hoạt <b>Strategy Pattern</b> cho hàm suy giảm (DecayStrategy)
 * và kỹ thuật cắt tỉa nhánh (PruningStrategy).</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class DefaultStreamMiner extends AbstractStreamMiner {

    private final DHOList globalList;
    private final List<Transaction> allTransactions;
    private int currentTL;

    public DefaultStreamMiner() {
        this.globalList = new DHOList();
        this.allTransactions = new ArrayList<>();
        this.currentTL = 0;
    }

    public DefaultStreamMiner(DHOList dhoList, List<Transaction> transactions, int currentTL) {
        this.globalList = dhoList != null ? dhoList : new DHOList();
        this.allTransactions = transactions != null ? transactions : new ArrayList<>();
        this.currentTL = currentTL;
    }

    @Override
    protected void validateBatch(List<Transaction> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        for (Transaction t : batch) {
            if (t == null) {
                throw new IllegalArgumentException("Giao dịch trong batch không được phép null");
            }
            if (t.getLength() <= 0) {
                throw new IllegalArgumentException("Giao dịch TID=" + t.getTid() + " có độ dài rỗng");
            }
        }
    }

    @Override
    protected void updateStructure(List<Transaction> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        allTransactions.addAll(batch);
        for (Transaction t : batch) {
            if (t.getTid() > currentTL) {
                currentTL = t.getTid();
            }
        }
        globalList.constructOrUpdate(batch);
    }

    @Override
    protected void calculateBounds(MiningConfig config) {
        // Tái cấu trúc Global DHO-List theo DecayStrategy và currentTL
        globalList.reconstruct(config.getDecayStrategy(), currentTL);
    }

    @Override
    protected List<PatternResult> minePatterns(MiningConfig config) {
        double minSup = config.resolveAbsoluteMinSup(allTransactions.size());
        DecayStrategy decayStrategy = config.getDecayStrategy();
        PruningStrategy pruningStrategy = config.getPruningStrategy();

        List<PatternResult> allVisited = new ArrayList<>();
        List<PatternResult> dhops = new ArrayList<>();

        List<DHONode> sortedNodes = globalList.getSortedNodes();
        List<String> processingOrder = new ArrayList<>();
        for (DHONode node : sortedNodes) {
            processingOrder.add(node.getItemName());
        }

        dfsMining(new ArrayList<>(), new ArrayList<>(), 0, processingOrder, allVisited, dhops,
                decayStrategy, pruningStrategy, currentTL, minSup, config.getMaxPatternLength());

        return allVisited;
    }

    private void dfsMining(
            List<String> prefixItems,
            List<Entry> prefixEntries,
            int startIdx,
            List<String> processingOrder,
            List<PatternResult> allVisited,
            List<PatternResult> dhops,
            DecayStrategy decayStrategy,
            PruningStrategy pruningStrategy,
            int TL,
            double minSup,
            int maxLen
    ) {
        for (int i = startIdx; i < processingOrder.size(); i++) {
            String item = processingOrder.get(i);
            DHONode itemNode = globalList.getNode(item);
            if (itemNode == null) continue;

            List<String> candPattern = new ArrayList<>(prefixItems);
            candPattern.add(item);
            if (candPattern.size() > maxLen) {
                continue;
            }

            String patternName = String.join("", candPattern);

            List<Entry> candEntries;
            if (prefixItems.isEmpty()) {
                candEntries = itemNode.getEntries();
            } else {
                candEntries = intersectEntries(prefixEntries, itemNode.getEntries());
            }

            if (candEntries.isEmpty()) {
                continue;
            }

            // 1. Tính DO bằng DecayStrategy
            double doVal = calculateDO(candEntries, candPattern.size(), decayStrategy, TL);

            // 2. Tính DUBO bằng DecayStrategy
            double duboVal = DUBOCalculator.calculate(candEntries, decayStrategy, TL);

            boolean isDHOP = (doVal >= minSup - 1e-9);
            boolean isPruned = pruningStrategy.shouldPrune(duboVal, minSup);

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

            // Mở rộng đệ quy DFS nếu không bị cắt tỉa
            if (!isPruned) {
                dfsMining(candPattern, candEntries, i + 1, processingOrder, allVisited, dhops,
                        decayStrategy, pruningStrategy, TL, minSup, maxLen);
            }
        }
    }

    public static double calculateDO(List<Entry> entries, int patternLength, DecayStrategy strategy, int TL) {
        if (patternLength <= 0) {
            throw new IllegalArgumentException("Pattern length must be positive");
        }
        double sum = 0.0;
        for (Entry e : entries) {
            double occ = (double) patternLength / e.tlen();
            double decay = strategy.calculateDecay(e.tid(), TL);
            sum += occ * decay;
        }
        return sum;
    }

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

    @Override
    public List<PatternResult> mine(MiningConfig config) {
        MiningCycleResult res = processBatchAndMine(Collections.emptyList(), config);
        return res.allVisitedPatterns();
    }

    @Override
    public DHOList getDHOList() {
        return globalList;
    }

    @Override
    public int getCurrentTL() {
        return currentTL;
    }

    public void setCurrentTL(int tl) {
        this.currentTL = tl;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(allTransactions);
    }

    @Override
    public void reset() {
        globalList.clear();
        allTransactions.clear();
        currentTL = 0;
    }
}

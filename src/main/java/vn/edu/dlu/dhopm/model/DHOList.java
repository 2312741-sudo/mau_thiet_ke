package vn.edu.dlu.dhopm.model;

import vn.edu.dlu.dhopm.pattern.memento.DHOListMemento;
import vn.edu.dlu.dhopm.pattern.strategy.DampedExponentialDecay;
import vn.edu.dlu.dhopm.pattern.strategy.DecayStrategy;

import java.util.*;

/**
 * Cấu trúc Global DHO-List quản lý các node của các item trong toàn bộ CSDL.
 *
 * <p>Đóng vai trò là <b>Originator</b> trong <b>Memento Pattern (GoF)</b>:
 * Có khả năng tạo ra bản sao trạng thái nội bộ ({@link DHOListMemento})
 * và phục hồi nguyên vẹn trạng thái từ Memento đó.</p>
 *
 * <p>Cung cấp 2 tiến trình cơ bản:
 * 1. constructOrUpdate: Quét một lần duy nhất (One-scan) để chèn các entry &lt;TID, TLen&gt;
 * 2. reconstruct: Tính lại DO theo TL và DecayStrategy, sắp xếp theo Support tăng dần
 * </p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class DHOList {
    private final Map<String, DHONode> nodeMap;
    private final List<DHONode> sortedNodes;

    public DHOList() {
        this.nodeMap = new LinkedHashMap<>();
        this.sortedNodes = new ArrayList<>();
    }

    /**
     * Pha 1: Xây dựng hoặc cập nhật Global DHO-List bằng cách quét 1 lần các giao dịch mới.
     *
     * @param transactions Danh sách các giao dịch cần nạp (DB0 hoặc các phần gia tăng DB1, DB2, ...)
     */
    public void constructOrUpdate(List<Transaction> transactions) {
        if (transactions == null) return;
        for (Transaction t : transactions) {
            int tid = t.getTid();
            int tlen = t.getLength();

            for (String item : t.getItems()) {
                DHONode node = nodeMap.computeIfAbsent(item, DHONode::new);
                node.addEntry(tid, tlen);
            }
        }
    }

    /**
     * Pha 2: Tái cấu trúc Global DHO-List với DecayStrategy (Strategy Pattern).
     *
     * @param strategy Chiến lược suy giảm trọng số (Damped, Landmark, Linear)
     * @param TL       Mã định danh giao dịch mới nhất đã quét
     */
    public void reconstruct(DecayStrategy strategy, int TL) {
        if (strategy == null) {
            throw new IllegalArgumentException("DecayStrategy không được phép null");
        }

        // 1. Tính lại DO cho từng node
        for (DHONode node : nodeMap.values()) {
            node.resetDO();
            for (Entry entry : node.getEntries()) {
                double occupancy = 1.0 / entry.tlen();
                double decay = strategy.calculateDecay(entry.tid(), TL);
                node.accumulateDO(occupancy * decay);
            }
        }

        // 2. Sắp xếp theo Support tăng dần (nếu bằng thì so sánh theo tên item)
        sortedNodes.clear();
        sortedNodes.addAll(nodeMap.values());
        sortedNodes.sort(Comparator.comparingInt(DHONode::getSupport)
                .thenComparing(DHONode::getItemName));
    }

    /**
     * Pha 2 (Tương thích ngược): Tái cấu trúc Global DHO-List theo hệ số f của bài báo gốc.
     *
     * @param f  Hệ số suy giảm (0 &lt; f &le; 1)
     * @param TL Mã định danh giao dịch mới nhất đã quét
     */
    public void reconstruct(double f, int TL) {
        if (!Double.isFinite(f) || f <= 0.0 || f > 1.0) {
            throw new IllegalArgumentException("Decay factor f must be in (0, 1]");
        }
        reconstruct(new DampedExponentialDecay(f), TL);
    }

    /**
     * Memento Pattern: Lưu lại ảnh chụp nhanh trạng thái nội bộ.
     *
     * @param currentTL        Mã thời gian TL hiện tại
     * @param allTransactions  Toàn bộ danh sách giao dịch
     * @param streamNextIndex  Chỉ số tiếp theo của bộ mô phỏng luồng
     * @return DHOListMemento lưu trữ trạng thái bất biến
     */
    public DHOListMemento saveToMemento(int currentTL, List<Transaction> allTransactions, int streamNextIndex) {
        return new DHOListMemento(nodeMap, sortedNodes, currentTL, allTransactions, streamNextIndex);
    }

    /**
     * Memento Pattern: Khôi phục lại trạng thái nội bộ từ ảnh chụp nhanh.
     *
     * @param memento Đối tượng snapshot chứa trạng thái trước đó
     */
    public void restoreFromMemento(DHOListMemento memento) {
        if (memento == null) return;

        this.nodeMap.clear();
        this.nodeMap.putAll(memento.getSavedNodeMap());

        this.sortedNodes.clear();
        for (String itemName : memento.getSavedSortedItemNames()) {
            DHONode node = this.nodeMap.get(itemName);
            if (node != null) {
                this.sortedNodes.add(node);
            }
        }
    }

    public DHONode getNode(String item) {
        return nodeMap.get(item);
    }

    public List<DHONode> getSortedNodes() {
        return Collections.unmodifiableList(sortedNodes);
    }

    public Collection<DHONode> getAllNodes() {
        return Collections.unmodifiableCollection(nodeMap.values());
    }

    public int getItemCount() {
        return nodeMap.size();
    }

    public void clear() {
        nodeMap.clear();
        sortedNodes.clear();
    }
}

package vn.edu.dlu.dhopm.pattern.memento;

import vn.edu.dlu.dhopm.model.DHONode;
import vn.edu.dlu.dhopm.model.Transaction;

import java.util.*;

/**
 * Memento Object trong <b>Memento Pattern (GoF)</b>.
 *
 * <p>Lưu trữ ảnh chụp nhanh (Snapshot) toàn bộ trạng thái bên trong của cấu trúc Global DHO-List
 * và ngữ cảnh thực thi luồng tại một thời điểm nhất định (trước khi nhận một giao dịch mới).</p>
 *
 * <p>Đặc tính: Bất biến (Immutable) đối với các đối tượng bên ngoài (Caretaker),
 * chỉ có Originator (DHOList / Engine) mới có quyền truy cập và giải nén trạng thái để khôi phục.</p>
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public final class DHOListMemento {

    private final Map<String, DHONode> savedNodeMap;
    private final List<String> savedSortedItemNames;
    private final int savedTL;
    private final List<Transaction> savedTransactions;
    private final int savedStreamNextIndex;
    private final long timestamp;

    public DHOListMemento(
            Map<String, DHONode> nodeMap,
            List<DHONode> sortedNodes,
            int currentTL,
            List<Transaction> allTransactions,
            int streamNextIndex
    ) {
        this.savedNodeMap = new LinkedHashMap<>();
        if (nodeMap != null) {
            for (Map.Entry<String, DHONode> entry : nodeMap.entrySet()) {
                this.savedNodeMap.put(entry.getKey(), new DHONode(entry.getValue()));
            }
        }

        this.savedSortedItemNames = new ArrayList<>();
        if (sortedNodes != null) {
            for (DHONode node : sortedNodes) {
                this.savedSortedItemNames.add(node.getItemName());
            }
        }

        this.savedTL = currentTL;
        this.savedTransactions = allTransactions != null ? new ArrayList<>(allTransactions) : new ArrayList<>();
        this.savedStreamNextIndex = streamNextIndex;
        this.timestamp = System.currentTimeMillis();
    }

    public Map<String, DHONode> getSavedNodeMap() {
        Map<String, DHONode> copy = new LinkedHashMap<>();
        for (Map.Entry<String, DHONode> entry : savedNodeMap.entrySet()) {
            copy.put(entry.getKey(), new DHONode(entry.getValue()));
        }
        return copy;
    }

    public List<String> getSavedSortedItemNames() {
        return Collections.unmodifiableList(savedSortedItemNames);
    }

    public int getSavedTL() {
        return savedTL;
    }

    public List<Transaction> getSavedTransactions() {
        return Collections.unmodifiableList(savedTransactions);
    }

    public int getSavedStreamNextIndex() {
        return savedStreamNextIndex;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "DHOListMemento[TL=" + savedTL + ", Items=" + savedNodeMap.size() 
                + ", Trans=" + savedTransactions.size() + ", NextIndex=" + savedStreamNextIndex + "]";
    }
}

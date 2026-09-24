package vn.edu.dlu.dhopm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Node trong danh sach DHO-List tuong ung voi mot item hoac mot mau ung vien.
 * Chua:
 * - name: ten item (hoac hau to cua pattern)
 * - doValue: gia tri Damped Occupancy tich luy
 * - support: do ho tro (so luong giao dich chua item)
 * - entries: danh sach cac entry <TID, TLen>
 */
public class DHONode {
    private final String itemName;
    private double doValue;
    private int support;
    private final List<Entry> entries;

    public DHONode(String itemName) {
        this.itemName = itemName;
        this.doValue = 0.0;
        this.support = 0;
        this.entries = new ArrayList<>();
    }

    /**
     * Copy constructor de sao chep sau trang thai DHONode phuc vu Memento Pattern.
     */
    public DHONode(DHONode other) {
        this.itemName = other.itemName;
        this.doValue = other.doValue;
        this.support = other.support;
        this.entries = new ArrayList<>(other.entries);
    }

    public void addEntry(int tid, int tlen) {
        addEntry(new Entry(tid, tlen));
    }

    public void addEntry(Entry entry) {
        this.entries.add(entry);
        this.entries.sort(java.util.Comparator.comparingInt(Entry::tid));
        this.support++;
    }

    public void resetDO() {
        this.doValue = 0.0;
    }

    public void accumulateDO(double value) {
        this.doValue += value;
    }

    public String getItemName() {
        return itemName;
    }

    public double getDoValue() {
        return doValue;
    }

    public void setDoValue(double doValue) {
        this.doValue = doValue;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public String toString() {
        return "Node[" + itemName + ", DO=" + String.format("%.4f", doValue) + ", Sup=" + support + ", Entries=" + entries + "]";
    }
}

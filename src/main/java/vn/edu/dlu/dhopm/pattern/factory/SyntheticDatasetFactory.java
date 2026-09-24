package vn.edu.dlu.dhopm.pattern.factory;

import vn.edu.dlu.dhopm.model.Transaction;

import java.util.*;

/**
 * Concrete Creator tự động sinh dữ liệu luồng giao dịch ngẫu nhiên (Synthetic Streaming Generator).
 *
 * <p>Mô phỏng nguồn dữ liệu IoT / Thương mại điện tử liên tục với kích thước và phân phối tùy biến.</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class SyntheticDatasetFactory extends DatasetLoaderFactory {

    private final int count;
    private final int startTid;
    private final List<String> itemAlphabet;
    private final Random random;

    public SyntheticDatasetFactory(int count, int startTid) {
        this(count, startTid, Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H"), new Random(42));
    }

    public SyntheticDatasetFactory(int count, int startTid, List<String> itemAlphabet, Random random) {
        this.count = count;
        this.startTid = startTid;
        this.itemAlphabet = itemAlphabet;
        this.random = random;
    }

    @Override
    public List<Transaction> createDataset() {
        List<Transaction> list = new ArrayList<>();
        int tid = startTid;

        for (int i = 0; i < count; i++) {
            int transLen = 2 + random.nextInt(4); // Do dai từ 2 den 5 items
            Set<String> chosen = new HashSet<>();
            while (chosen.size() < transLen) {
                String item = itemAlphabet.get(random.nextInt(itemAlphabet.size()));
                chosen.add(item);
            }
            List<String> items = new ArrayList<>(chosen);
            Collections.sort(items);
            list.add(new Transaction(tid++, items));
        }

        return list;
    }

    @Override
    public String getDatasetName() {
        return "Luồng dữ liệu tổng hợp ngẫu nhiên (" + count + " Giao dịch)";
    }

    @Override
    public String getDatasetDescription() {
        return "Sinh " + count + " giao dịch ngẫu nhiên từ bảng chữ cái items " + itemAlphabet + ".";
    }
}

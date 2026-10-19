package vn.edu.dlu.dhopm.pattern.factory;

import vn.edu.dlu.dhopm.core.DatasetLoader;
import vn.edu.dlu.dhopm.model.Transaction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Creator nạp dữ liệu giao dịch từ File CSV hoặc chuỗi text.
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class CsvDatasetFactory extends DatasetLoaderFactory {

    private final InputStream inputStream;
    private final String sourceName;

    public CsvDatasetFactory(InputStream inputStream, String sourceName) {
        this.inputStream = inputStream;
        this.sourceName = sourceName;
    }

    public CsvDatasetFactory(File file) throws FileNotFoundException {
        this(new FileInputStream(file), file.getName());
    }

    public CsvDatasetFactory(String textContent, String sourceName) {
        this(new ByteArrayInputStream(textContent.getBytes(StandardCharsets.UTF_8)), sourceName);
    }

    @Override
    public List<Transaction> createDataset() {
        if (inputStream == null) return Collections.emptyList();
        return DatasetLoader.loadFromStream(inputStream);
    }

    @Override
    public String getDatasetName() {
        return "Nguồn dữ liệu tệp CSV: " + sourceName;
    }

    @Override
    public String getDatasetDescription() {
        return "Nạp các giao dịch từ nguồn ngoài hoặc tệp CSV/Text định dạng: [TID] item1 item2 ...";
    }
}

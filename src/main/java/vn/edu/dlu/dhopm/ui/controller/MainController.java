package vn.edu.dlu.dhopm.ui.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import vn.edu.dlu.dhopm.model.DHONode;
import vn.edu.dlu.dhopm.model.PatternResult;
import vn.edu.dlu.dhopm.pattern.facade.StreamMiningFacade;
import vn.edu.dlu.dhopm.pattern.factory.*;
import vn.edu.dlu.dhopm.pattern.strategy.*;
import vn.edu.dlu.dhopm.pattern.template.MiningCycleResult;
import vn.edu.dlu.dhopm.ui.component.DHONodeCard;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller điều khiển toàn bộ giao diện JavaFX cho DHOPM Pattern Studio.
 *
 * <p>Được tái cấu trúc theo mô hình <b>Clean Architecture</b> kết hợp <b>Facade Pattern (GoF)</b>:
 * Tầng UI Controller chỉ tương tác duy nhất với {@link StreamMiningFacade}, loại bỏ hoàn toàn
 * việc quản lý luồng ngầm (Threads), giải thuật thô hay mã lệnh rẽ nhánh phức tạp.</p>
 *
 * <p>Tích hợp trực quan 10 Mẫu Thiết Kế GoF:
 * <ul>
 *   <li><b>Factory Method:</b> ComboBox chọn nguồn dữ liệu (Bài báo, DB0, Synthetic, Lab 1 Custom)</li>
 *   <li><b>Strategy Pattern:</b> ComboBox hoán đổi giải thuật suy giảm và cắt tỉa tại runtime</li>
 *   <li><b>Command &amp; Memento:</b> Nút Undo / Redo kèm bảng Inspector xem ngăn xếp snapshot trực tiếp</li>
 *   <li><b>State Pattern:</b> Tự động kiểm soát tính khả dụng (Disable/Enable) của các nút chức năng</li>
 *   <li><b>Decorator Pattern:</b> Đo đạc thời gian chạy vi giây (µs) và RAM từ BenchmarkMinerDecorator</li>
 *   <li><b>Template Method:</b> Khung xương 5 bước trực quan hóa trên Tab Pattern Studio</li>
 *   <li><b>Observer Pattern:</b> Danh sách nhật ký kiểm toán (Audit Trail) cập nhật tự động</li>
 * </ul>
 * </p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public class MainController implements Initializable {

    // Factory Method Controls
    @FXML private ComboBox<DatasetLoaderFactory> comboDatasetFactory;
    @FXML private Button btnLoadDataset;

    // Strategy & Parameter Controls
    @FXML private ComboBox<DecayStrategy> comboDecayStrategy;
    @FXML private ComboBox<PruningStrategy> comboPruningStrategy;
    @FXML private VBox boxDecayFactor;
    @FXML private Slider sliderF;
    @FXML private Slider sliderMinSup;
    @FXML private Label lblFValue;
    @FXML private Label lblMinSupValue;
    @FXML private Label lblAbsoluteMinSup;

    // Stream & Engine Status Labels
    @FXML private Label lblTL;
    @FXML private Label lblTransCount;
    @FXML private Label lblDhopCount;
    @FXML private Label lblVisitedCount;
    @FXML private Label lblPrunedCount;
    @FXML private Label lblStreamStatus;
    @FXML private Label lblSortOrder;

    // Benchmark & State Pattern Labels
    @FXML private Label lblBenchmarkTime;
    @FXML private Label lblBenchmarkMemory;
    @FXML private Label lblUndoStackInfo;

    // Action Buttons
    @FXML private Button btnStartStream;
    @FXML private Button btnPauseStream;
    @FXML private Button btnStepStream;
    @FXML private Button btnUndo;
    @FXML private Button btnRedo;
    @FXML private Button btnReset;

    // Visual Views
    @FXML private FlowPane dhoNodesPane;
    @FXML private TableView<PatternResult> tableResults;
    @FXML private TableColumn<PatternResult, String> colPattern;
    @FXML private TableColumn<PatternResult, Number> colSupport;
    @FXML private TableColumn<PatternResult, String> colDO;
    @FXML private TableColumn<PatternResult, String> colDUBO;
    @FXML private TableColumn<PatternResult, String> colStatus;
    @FXML private TableColumn<PatternResult, String> colTransactions;

    @FXML private LineChart<String, Number> lineChartDO;

    // GoF Pattern Studio Tab
    @FXML private ListView<String> listMementoSnapshots;
    @FXML private ListView<String> listAuditLogs;
    @FXML private Button btnClearAuditLogs;

    // Facade Object
    private StreamMiningFacade facade;
    private final ObservableList<PatternResult> tableData = FXCollections.observableArrayList();
    private final ObservableList<String> auditLogData = FXCollections.observableArrayList();
    private final ObservableList<String> mementoStackData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFacade();
        initFactoryComboBox();
        initStrategyComboBoxes();
        initTableView();
        initPatternStudioLists();
        initEventHandlers();

        // Nạp dữ liệu ban đầu từ bài báo gốc
        facade.initializeDefaultPaperData();
        renderUI();
    }

    private void initFacade() {
        facade = new StreamMiningFacade();
        facade.setOnUIUpdate(() -> Platform.runLater(this::renderUI));
    }

    private void initFactoryComboBox() {
        comboDatasetFactory.getItems().addAll(
                new PaperDatasetFactory(),
                new DB0DatasetFactory(),
                new SyntheticDatasetFactory(50, 9),
                new CustomLab1DatasetFactory()
        );
        comboDatasetFactory.getSelectionModel().selectFirst();
        comboDatasetFactory.setConverter(new StringConverter<>() {
            @Override
            public String toString(DatasetLoaderFactory factory) {
                return factory != null ? factory.getDatasetName() : "";
            }

            @Override
            public DatasetLoaderFactory fromString(String string) {
                return null;
            }
        });
    }

    private void initStrategyComboBoxes() {
        // 1. Decay Strategies
        comboDecayStrategy.getItems().addAll(
                new DampedExponentialDecay(0.90),
                new LandmarkDecay(),
                new LinearDecay(0.05)
        );
        comboDecayStrategy.getSelectionModel().selectFirst();
        comboDecayStrategy.setConverter(new StringConverter<>() {
            @Override
            public String toString(DecayStrategy strategy) {
                return strategy != null ? strategy.getName() : "";
            }

            @Override
            public DecayStrategy fromString(String string) {
                return null;
            }
        });

        // 2. Pruning Strategies
        comboPruningStrategy.getItems().addAll(
                new DUBOPruningStrategy(),
                new NoPruningStrategy()
        );
        comboPruningStrategy.getSelectionModel().selectFirst();
        comboPruningStrategy.setConverter(new StringConverter<>() {
            @Override
            public String toString(PruningStrategy strategy) {
                return strategy != null ? strategy.getName() : "";
            }

            @Override
            public PruningStrategy fromString(String string) {
                return null;
            }
        });
    }

    private void initTableView() {
        colPattern.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().pattern()));
        colSupport.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().support()));
        colDO.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDO()));
        colDUBO.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedDUBO()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colTransactions.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().transactionIds().stream()
                        .map(tid -> "T" + tid)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("")
        ));

        tableResults.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PatternResult item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isDHOP()) {
                    setStyle("-fx-background-color: #dcfce7; -fx-font-weight: bold;");
                } else if (item.isPruned()) {
                    setStyle("-fx-background-color: #fee2e2;");
                } else {
                    setStyle("");
                }
            }
        });

        tableResults.setItems(tableData);
    }

    private void initPatternStudioLists() {
        if (listMementoSnapshots != null) {
            listMementoSnapshots.setItems(mementoStackData);
        }
        if (listAuditLogs != null) {
            listAuditLogs.setItems(auditLogData);
        }
    }

    private void initEventHandlers() {
        // Factory Method Button
        btnLoadDataset.setOnAction(e -> {
            DatasetLoaderFactory selectedFactory = comboDatasetFactory.getValue();
            if (selectedFactory != null) {
                facade.loadDatasetFromFactory(selectedFactory);
            }
        });

        // Strategy ComboBoxes
        comboDecayStrategy.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean isExponential = newVal instanceof DampedExponentialDecay;
                boxDecayFactor.setDisable(!isExponential);
                boxDecayFactor.setOpacity(isExponential ? 1.0 : 0.4);
                facade.setDecayStrategy(newVal);
            }
        });

        comboPruningStrategy.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                facade.setPruningStrategy(newVal);
            }
        });

        // Sliders
        sliderF.valueProperty().addListener((obs, oldVal, newVal) -> {
            double f = Math.round(newVal.doubleValue() * 100.0) / 100.0;
            lblFValue.setText(String.format("%.2f", f));
            if (comboDecayStrategy.getValue() instanceof DampedExponentialDecay) {
                facade.setDecayFactor(f);
            }
        });

        sliderMinSup.valueProperty().addListener((obs, oldVal, newVal) -> {
            double ratio = Math.round(newVal.doubleValue() * 100.0) / 100.0;
            lblMinSupValue.setText(String.format("%.0f%%", ratio * 100));
            facade.setMinSupRatio(ratio);
        });

        // Stream Control Buttons (State & Command Patterns)
        btnStartStream.setOnAction(e -> facade.startStream());
        btnPauseStream.setOnAction(e -> facade.pauseStream());
        btnStepStream.setOnAction(e -> {
            boolean stepped = facade.stepNext();
            if (!stepped) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã bơm hết toàn bộ giao dịch trong luồng mẫu!");
                alert.show();
            }
        });

        // Undo & Redo Buttons (Command + Memento Pattern)
        btnUndo.setOnAction(e -> {
            boolean undone = facade.undo();
            if (!undone) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Không còn thao tác nào trong ngăn xếp để Undo!");
                alert.show();
            }
        });

        btnRedo.setOnAction(e -> {
            boolean redone = facade.redo();
            if (!redone) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Không còn thao tác nào để Redo!");
                alert.show();
            }
        });

        // Reset Button
        btnReset.setOnAction(e -> facade.initializeDefaultPaperData());

        // Clear Audit Logs Button
        if (btnClearAuditLogs != null) {
            btnClearAuditLogs.setOnAction(e -> {
                facade.getAuditLogObserver().clear();
                auditLogData.clear();
            });
        }
    }

    /**
     * Đồng bộ hóa và kết xuất dữ liệu lên toàn bộ giao diện từ Facade.
     */
    public void renderUI() {
        int totalTrans = facade.getAllTransactions().size();
        int TL = facade.getCurrentTL();
        double minSup = facade.getCurrentConfig().resolveAbsoluteMinSup(totalTrans);

        // 1. Cập nhật trạng thái máy State Pattern
        lblStreamStatus.setText(facade.getState().getStatusMessage());
        lblStreamStatus.setStyle("-fx-text-fill: " + facade.getState().getStatusColor() + ";");

        // Kiểm soát tính khả dụng của các nút bấm an toàn
        btnStartStream.setDisable(!facade.getState().isStartAllowed());
        btnPauseStream.setDisable(!facade.getState().isPauseAllowed());
        btnStepStream.setDisable(!facade.getState().isStepAllowed());
        btnReset.setDisable(!facade.getState().isResetAllowed());
        btnUndo.setDisable(!facade.canUndo());
        btnRedo.setDisable(!facade.canRedo());

        // 2. Cập nhật các nhãn thống kê
        lblAbsoluteMinSup.setText(String.format("%.2f", minSup));
        lblTL.setText("T" + TL);
        lblTransCount.setText(String.valueOf(totalTrans));

        // 3. Khai phá và lấy kết quả qua Decorator
        MiningCycleResult result = facade.recalculateAndMine();
        List<PatternResult> results = result.allVisitedPatterns();
        List<PatternResult> dhops = result.dhopPatterns();

        lblDhopCount.setText(dhops.size() + " mẫu");
        lblVisitedCount.setText(results.size() + " mẫu");

        long prunedCount = results.stream().filter(PatternResult::isPruned).count();
        double prunedRatio = results.isEmpty() ? 0.0 : ((double) prunedCount / results.size()) * 100;
        lblPrunedCount.setText(prunedCount + " mẫu (" + String.format("%.1f%%", prunedRatio) + ")");

        // 4. Cập nhật thông số từ Benchmark Decorator
        lblBenchmarkTime.setText(String.format("%.1f µs", facade.getBenchmarkDecorator().getLastExecutionTimeMicros()));
        lblBenchmarkMemory.setText(String.format("%.1f KB", facade.getBenchmarkDecorator().getLastMemoryUsedKb()));

        // 5. Cập nhật Memento Stack Info
        lblUndoStackInfo.setText("Memento Stack: " + facade.getUndoCount() + " snapshots | Redo: " + facade.getRedoCount());

        // 6. Cập nhật thứ tự sắp xếp Support tăng dần
        List<DHONode> sortedNodes = facade.getGlobalList().getSortedNodes();
        StringBuilder orderSb = new StringBuilder();
        for (int i = 0; i < sortedNodes.size(); i++) {
            orderSb.append(sortedNodes.get(i).getItemName())
                    .append(" (S=").append(sortedNodes.get(i).getSupport()).append(")");
            if (i < sortedNodes.size() - 1) {
                orderSb.append(" ≺ ");
            }
        }
        lblSortOrder.setText(orderSb.toString());

        // 7. Hiển thị danh sách DHO-List Nodes
        dhoNodesPane.getChildren().clear();
        for (DHONode node : sortedNodes) {
            dhoNodesPane.getChildren().add(new DHONodeCard(node, facade.getCurrentConfig().getDecayStrategy(), TL, minSup));
        }

        // 8. Cập nhật bảng kết quả TableView
        tableData.setAll(results);

        // 9. Cập nhật Biểu đồ Line Chart
        updateChart(sortedNodes, minSup);

        // 10. Cập nhật Memento Snapshots List & Audit Log
        if (listMementoSnapshots != null) {
            List<String> snapshots = facade.getUndoStackDescriptions();
            if (snapshots.isEmpty()) {
                mementoStackData.setAll(List.of("(Ngăn xếp Memento trống - Chưa có snapshot hoàn tác)"));
            } else {
                mementoStackData.setAll(snapshots);
            }
        }

        if (listAuditLogs != null) {
            auditLogData.setAll(facade.getAuditLogObserver().getLogEntries());
            if (!auditLogData.isEmpty()) {
                listAuditLogs.scrollTo(auditLogData.size() - 1);
            }
        }
    }

    private void updateChart(List<DHONode> sortedNodes, double minSup) {
        lineChartDO.getData().clear();

        XYChart.Series<String, Number> itemSeries = new XYChart.Series<>();
        itemSeries.setName("DO(Items) [" + facade.getCurrentConfig().getDecayStrategy().getName() + "]");
        for (DHONode node : sortedNodes) {
            itemSeries.getData().add(new XYChart.Data<>(node.getItemName(), node.getDoValue()));
        }

        XYChart.Series<String, Number> minSupSeries = new XYChart.Series<>();
        minSupSeries.setName("Ngưỡng minSup (" + String.format("%.2f", minSup) + ")");
        for (DHONode node : sortedNodes) {
            minSupSeries.getData().add(new XYChart.Data<>(node.getItemName(), minSup));
        }

        lineChartDO.getData().addAll(itemSeries, minSupSeries);
    }
}

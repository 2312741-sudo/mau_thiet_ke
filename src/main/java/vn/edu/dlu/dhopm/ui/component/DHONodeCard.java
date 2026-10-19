package vn.edu.dlu.dhopm.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import vn.edu.dlu.dhopm.core.DUBOCalculator;
import vn.edu.dlu.dhopm.model.DHONode;
import vn.edu.dlu.dhopm.model.Entry;
import vn.edu.dlu.dhopm.pattern.strategy.DampedExponentialDecay;
import vn.edu.dlu.dhopm.pattern.strategy.DecayStrategy;

/**
 * Custom UI Component biểu diễn một Node trong Global DHO-List
 * với giao diện Cyber-Card hiện đại, thanh đo độ chiếm dụng và trạng thái trực quan.
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class DHONodeCard extends VBox {

    public DHONodeCard(DHONode node, DecayStrategy strategy, int TL, double minSup) {
        setPadding(new Insets(12));
        setSpacing(8);
        setPrefWidth(260);
        getStyleClass().add("dho-node-card");

        double dubo = DUBOCalculator.calculate(node.getEntries(), strategy, TL);
        boolean isDhop = node.getDoValue() >= minSup - 1e-9;
        boolean isPruned = dubo < minSup - 1e-9;

        if (isDhop) {
            getStyleClass().add("dho-node-dhop");
        } else if (isPruned) {
            getStyleClass().add("dho-node-pruned");
        }

        buildCardContent(node, dubo, minSup, isDhop, isPruned);
    }

    public DHONodeCard(DHONode node, double f, int TL, double minSup) {
        this(node, new DampedExponentialDecay(f), TL, minSup);
    }

    private void buildCardContent(DHONode node, double dubo, double minSup, boolean isDhop, boolean isPruned) {
        // Header: Item Name, Status Pill & Support Badge
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(node.getItemName());
        nameLabel.getStyleClass().add("node-name-badge");

        Label statusPill = new Label(isDhop ? "DHOP ★" : (isPruned ? "Cắt tỉa ✂" : "Mở rộng ➔"));
        statusPill.getStyleClass().add(isDhop ? "pill-dhop" : (isPruned ? "pill-pruned" : "pill-expand"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label supBadge = new Label("Sup: " + node.getSupport());
        supBadge.getStyleClass().add("badge-sup");

        header.getChildren().addAll(nameLabel, statusPill, spacer, supBadge);

        // DO Row
        HBox doRow = new HBox();
        doRow.setAlignment(Pos.CENTER_LEFT);
        Label doTitle = new Label("DO(" + node.getItemName() + "):");
        doTitle.getStyleClass().add("stat-title");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Label doVal = new Label(String.format("%.4f", node.getDoValue()));
        doVal.getStyleClass().add(isDhop ? "stat-val-dhop" : "stat-val");
        doRow.getChildren().addAll(doTitle, spacer1, doVal);

        // Progress Bar DO vs minSup
        double ratio = minSup > 0 ? Math.min(1.0, node.getDoValue() / minSup) : 0.0;
        ProgressBar pbDO = new ProgressBar(ratio);
        pbDO.setMaxWidth(Double.MAX_VALUE);
        pbDO.getStyleClass().add(isDhop ? "progress-dhop" : "progress-normal");

        // DUBO Row
        HBox duboRow = new HBox();
        duboRow.setAlignment(Pos.CENTER_LEFT);
        Label duboTitle = new Label("DUBO(" + node.getItemName() + "):");
        duboTitle.getStyleClass().add("stat-title");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Label duboVal = new Label(String.format("%.4f", dubo));
        duboVal.getStyleClass().add(isPruned ? "stat-val-pruned" : "stat-val");
        duboRow.getChildren().addAll(duboTitle, spacer2, duboVal);

        // Entries Row
        StringBuilder sb = new StringBuilder();
        for (Entry e : node.getEntries()) {
            sb.append("<").append(e.tid()).append(",").append(e.tlen()).append("> ");
        }
        Label entriesLabel = new Label("Entries: " + sb.toString().trim());
        entriesLabel.getStyleClass().add("entries-label");
        entriesLabel.setWrapText(true);

        getChildren().addAll(header, doRow, pbDO, duboRow, entriesLabel);
    }
}

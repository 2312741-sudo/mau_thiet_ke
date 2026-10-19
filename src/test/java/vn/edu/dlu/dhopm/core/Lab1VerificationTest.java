package vn.edu.dlu.dhopm.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.dlu.dhopm.model.DHONode;
import vn.edu.dlu.dhopm.model.PatternResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bo kiem thu tu dong xac thuc 100% ket qua giua chuong trinh Java (Lab 2)
 * va ket qua tinh toan chay tay trong file DHOPM_TestCases.xlsx (Lab 1).
 *
 * Sinh vien: Vo Cong Vinh (2312800), Nguyen Duc Tin (2312774), Ngo Quoc Truong Vi (2312796)
 * Truong: Dai Hoc Da Lat (DLU) - Khoa Cong Nghe Thong Tin
 */
public class Lab1VerificationTest {

    private DHOPMEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DHOPMEngine();
    }

    @Test
    @DisplayName("Lab 1 - Items_DO: Kiem tra DO va DUBO cua 7 item don le tren CSDL goc (TL=8, f=0.9)")
    void testItems_DO_And_DUBO_MatchLab1() {
        engine.phase1_constructOrUpdate(DatasetLoader.getPaperDataset());
        engine.phase2_reconstruct(0.9, 8);

        // Kiem tra tung item theo bang Items_DO trong Lab 1:
        // Item G: Sup=1, DO=0.3333, DUBO=1.0
        DHONode nodeG = engine.getGlobalList().getNode("G");
        assertNotNull(nodeG);
        assertEquals(1, nodeG.getSupport());
        assertEquals(0.3333, nodeG.getDoValue(), 0.0005);
        assertEquals(1.0, DUBOCalculator.calculate(nodeG.getEntries(), 0.9, 8), 0.0005);

        // Item B: Sup=3, DO=0.7371, DUBO=1.8
        DHONode nodeB = engine.getGlobalList().getNode("B");
        assertNotNull(nodeB);
        assertEquals(3, nodeB.getSupport());
        assertEquals(0.7371, nodeB.getDoValue(), 0.002);
        assertEquals(1.8, DUBOCalculator.calculate(nodeB.getEntries(), 0.9, 8), 0.0005);

        // Item A: Sup=4, DO=0.8551, DUBO=3.5
        DHONode nodeA = engine.getGlobalList().getNode("A");
        assertNotNull(nodeA);
        assertEquals(4, nodeA.getSupport());
        assertEquals(0.8551, nodeA.getDoValue(), 0.0005);
        assertEquals(3.5, DUBOCalculator.calculate(nodeA.getEntries(), 0.9, 8), 0.0005);

        // Item C: Sup=4, DO=0.7109, DUBO=2.7
        DHONode nodeC = engine.getGlobalList().getNode("C");
        assertNotNull(nodeC);
        assertEquals(4, nodeC.getSupport());
        assertEquals(0.7109, nodeC.getDoValue(), 0.0005);
        assertEquals(2.7, DUBOCalculator.calculate(nodeC.getEntries(), 0.9, 8), 0.0005);

        // Item D: Sup=4, DO=0.7559, DUBO=2.835
        DHONode nodeD = engine.getGlobalList().getNode("D");
        assertNotNull(nodeD);
        assertEquals(4, nodeD.getSupport());
        assertEquals(0.7559, nodeD.getDoValue(), 0.0005);
        assertEquals(2.835, DUBOCalculator.calculate(nodeD.getEntries(), 0.9, 8), 0.001);

        // Item E: Sup=5, DO=1.0477, DUBO=4.5
        DHONode nodeE = engine.getGlobalList().getNode("E");
        assertNotNull(nodeE);
        assertEquals(5, nodeE.getSupport());
        assertEquals(1.0477, nodeE.getDoValue(), 0.0005);
        assertEquals(4.5, DUBOCalculator.calculate(nodeE.getEntries(), 0.9, 8), 0.0005);

        // Item F: Sup=5, DO=1.2553, DUBO=3.0375
        DHONode nodeF = engine.getGlobalList().getNode("F");
        assertNotNull(nodeF);
        assertEquals(5, nodeF.getSupport());
        assertEquals(1.2553, nodeF.getDoValue(), 0.0005);
        assertEquals(3.0375, DUBOCalculator.calculate(nodeF.getEntries(), 0.9, 8), 0.001);
    }

    @Test
    @DisplayName("Lab 1 - TC1: Bai bao goc (f=0.9, ∂=15%, minSup=1.2, TL=8) -> 2 DHOP: {AE, F}")
    void testTC1_StandardPaper() {
        engine.phase1_constructOrUpdate(DatasetLoader.getPaperDataset());
        List<PatternResult> results = engine.phase3_mine(1.2, 0.9, 8);

        List<PatternResult> dhops = results.stream().filter(PatternResult::isDHOP).toList();
        assertEquals(2, dhops.size(), "TC1 phai co dung 2 mau DHOP");

        Map<String, PatternResult> map = dhops.stream().collect(Collectors.toMap(PatternResult::pattern, p -> p));
        assertTrue(map.containsKey("AE"), "Phai chua mau AE");
        assertTrue(map.containsKey("F"), "Phai chua mau F");

        assertEquals(1.2601, map.get("AE").doValue(), 0.001);
        assertEquals(1.2553, map.get("F").doValue(), 0.001);
    }

    @Test
    @DisplayName("Lab 1 - TC2: Nguong cao ∂=20% (minSup=1.6, f=0.9, TL=8) -> 0 DHOP")
    void testTC2_HighMinSup() {
        engine.phase1_constructOrUpdate(DatasetLoader.getPaperDataset());
        List<PatternResult> results = engine.phase3_mine(1.6, 0.9, 8);

        List<PatternResult> dhops = results.stream().filter(PatternResult::isDHOP).toList();
        assertEquals(0, dhops.size(), "TC2 khong co mau nao dat minSup=1.6");
    }

    @Test
    @DisplayName("Lab 1 - TC3: Nguong thap ∂=10% (minSup=0.8, f=0.9, TL=8) -> 15 DHOPs")
    void testTC3_LowMinSup() {
        engine.phase1_constructOrUpdate(DatasetLoader.getPaperDataset());
        List<PatternResult> results = engine.phase3_mine(0.8, 0.9, 8);

        List<PatternResult> dhops = results.stream().filter(PatternResult::isDHOP).toList();
        assertEquals(15, dhops.size(), "TC3 phai co dung 15 mau DHOP");

        List<String> names = dhops.stream().map(PatternResult::pattern).toList();
        assertTrue(names.contains("GAE"));
        assertTrue(names.contains("BACF"));
        assertTrue(names.contains("BF"));
        assertTrue(names.contains("A"));
        assertTrue(names.contains("AE"));
        assertTrue(names.contains("AF"));
        assertTrue(names.contains("CD"));
        assertTrue(names.contains("CDE"));
        assertTrue(names.contains("CF"));
        assertTrue(names.contains("DE"));
        assertTrue(names.contains("DEF"));
        assertTrue(names.contains("DF"));
        assertTrue(names.contains("E"));
        assertTrue(names.contains("EF"));
        assertTrue(names.contains("F"));
    }

    @Test
    @DisplayName("Lab 1 - TC4: Suy giam nhanh f=0.8 (minSup=1.2, TL=8) -> 0 DHOP")
    void testTC4_HighDecay() {
        engine.phase1_constructOrUpdate(DatasetLoader.getPaperDataset());
        List<PatternResult> results = engine.phase3_mine(1.2, 0.8, 8);

        List<PatternResult> dhops = results.stream().filter(PatternResult::isDHOP).toList();
        assertEquals(0, dhops.size(), "TC4 khong co mau nao dat nguong do suy giam nhanh f=0.8");
    }

    @Test
    @DisplayName("Lab 1 - TC5: Khong suy giam f=1.0 (Landmark Window, minSup=1.2, TL=8) -> 9 DHOPs")
    void testTC5_NoDecay_Landmark() {
        engine.phase1_constructOrUpdate(DatasetLoader.getPaperDataset());
        List<PatternResult> results = engine.phase3_mine(1.2, 1.0, 8);

        List<PatternResult> dhops = results.stream().filter(PatternResult::isDHOP).toList();
        assertEquals(9, dhops.size(), "TC5 phai co dung 9 mau DHOP khi f=1.0");

        List<String> names = dhops.stream().map(PatternResult::pattern).toList();
        assertTrue(names.contains("BF"));
        assertTrue(names.contains("AE"));
        assertTrue(names.contains("CD"));
        assertTrue(names.contains("CDE"));
        assertTrue(names.contains("DE"));
        assertTrue(names.contains("DF"));
        assertTrue(names.contains("E"));
        assertTrue(names.contains("EF"));
        assertTrue(names.contains("F"));
    }

    @Test
    @DisplayName("Lab 1 - TC6: Co so du lieu DB0 (T1..T4, f=0.9, ∂=25%, minSup=1.0, TL=4) -> 3 DHOPs")
    void testTC6_DB0_Incremental() {
        engine.phase1_constructOrUpdate(DatasetLoader.getDB0Dataset());
        List<PatternResult> results = engine.phase3_mine(1.0, 0.9, 4);

        List<PatternResult> dhops = results.stream().filter(PatternResult::isDHOP).toList();
        assertEquals(3, dhops.size(), "TC6 tren DB0 phai co 3 mau DHOP");

        List<String> names = dhops.stream().map(PatternResult::pattern).toList();
        assertTrue(names.contains("CD"));
        assertTrue(names.contains("CDE"));
    }

    @Test
    @DisplayName("Lab 1 - TC7: Tap du lieu tuy chinh TC7_Custom (10 GD, f=0.9, minSup=1.5, TL=10) -> 9 DHOPs")
    void testTC7_CustomDataset() {
        engine.phase1_constructOrUpdate(DatasetLoader.getTC7CustomDataset());
        List<PatternResult> results = engine.phase3_mine(1.5, 0.9, 10);

        List<PatternResult> dhops = results.stream().filter(PatternResult::isDHOP).toList();
        assertEquals(9, dhops.size(), "TC7 phai co 9 mau dat DO >= 1.5");

        Map<String, PatternResult> map = dhops.stream().collect(Collectors.toMap(PatternResult::pattern, p -> p));
        // Kiem tra gia tri DO khop voi file Excel Lab 1
        assertEquals(1.8922, map.get("A").doValue(), 0.001);
        assertEquals(2.0495, map.get("B").doValue(), 0.001);
        assertEquals(1.6364, map.get("C").doValue(), 0.001);
        assertEquals(2.5240, map.get("AB").doValue(), 0.001);
        assertEquals(2.3540, map.get("AC").doValue(), 0.001);
        assertEquals(1.6403, map.get("ACB").doValue(), 0.001);
        assertEquals(1.8702, map.get("DB").doValue(), 0.001);
        assertEquals(1.8212, map.get("DCB").doValue(), 0.001);
        assertEquals(2.0124, map.get("CB").doValue(), 0.001);
    }
}

# ⚡ DHOPM Stream Visualizer • Design Patterns Studio
> **Đồ Án Môn Học:** Mẫu Thiết Kế (Design Patterns)  
> **Trường:** Đại Học Đà Lạt (DLU) - Khoa Công Nghệ Thông Tin  
> **Sinh viên thực hiện:** Nguyễn Thanh Tâm (MSSV: 2312741)  
> **Dựa trên bài báo khoa học:** *"Damped window based high occupancy pattern mining with one scanning of data streams"*, Engineering Applications of Artificial Intelligence (EAAI), Vol. 174, 2026. DOI: [10.1016/j.engappai.2026.114511](https://doi.org/10.1016/j.engappai.2026.114511)

---

## 📖 1. Giới Thiệu Đề Tài

Thuật toán **DHOPM** *(Damped High Occupancy Pattern Mining)* giải quyết bài toán khai phá các tổ hợp mục chiếm tỷ lệ lớn trong giao dịch (High Occupancy) trên **luồng dữ liệu thời gian thực (Data Streams)**:
- **Độ đo suy giảm theo thời gian (Damped Occupancy - DO):** Nhân thêm hệ số suy giảm $f^{T_L - T_d}$ ($0 < f < 1$), giúp dữ liệu cũ dần mất giá trị và ưu tiên các xu hướng mới nhất.
- **Cấu trúc DHO-List một lần quét (One-Scan):** Duy trì và cập nhật dữ liệu gia tăng tức thì trong bộ nhớ RAM mà không cần đọc lại các giao dịch cũ.
- **Cận trên DUBO (Damped Upper Bound Occupancy):** Cắt tỉa không gian tìm kiếm cực kỳ hiệu quả mà vẫn đảm bảo tính đúng đắn 100% (không bỏ sót bất kỳ mẫu DHOP nào).

Dự án được tái cấu trúc thành một **Framework Khai Phá Dữ Liệu Luồng Đa Chiến Lược (Extensible Stream Mining Framework)** ứng dụng triệt để **10 Mẫu Thiết Kế GoF (Gang of Four)** kết hợp nguyên lý **SOLID** và giao diện **Cyber-Engineering Dark Mode JavaFX**.

---

## 🏛️ 2. Hệ Thống 10 Mẫu Thiết Kế GoF Được Áp Dụng

Dự án áp dụng chặt chẽ 10 Design Patterns GoF vào đúng bài toán kỹ thuật luồng dữ liệu:

| STT | Mẫu Thiết Kế (Pattern) | Nhóm GoF | Thành phần trong mã nguồn | Vai trò kỹ thuật thực tế |
|:---:|:---|:---:|:---|:---|
| 1 | **Strategy** | Behavioral | `vn.edu.dlu.dhopm.pattern.strategy` | Hoán đổi linh hoạt cơ chế suy giảm (`DampedExponentialDecay`, `LandmarkDecay`, `LinearDecay`) và cắt tỉa (`DUBOPruningStrategy`, `NoPruningStrategy`). |
| 2 | **Template Method** | Behavioral | `vn.edu.dlu.dhopm.pattern.template` | `AbstractStreamMiner` định khung quy trình khai phá 5 bước bất biến (`validateBatch` ➔ `updateStructure` ➔ `calculateBounds` ➔ `minePatterns` ➔ `notifyObservers`). |
| 3 | **Observer** | Behavioral | `vn.edu.dlu.dhopm.pattern.observer` | Tách rời luồng dữ liệu: `StreamObserver`, `MiningObserver`, `AuditLogObserver`, `BenchmarkMetricsObserver`. |
| 4 | **Command** | Behavioral | `vn.edu.dlu.dhopm.pattern.command` | Đóng gói thao tác thành đối tượng (`StepNextCommand`, `InjectBatchCommand`, `ResetCommand`) hỗ trợ **Undo / Redo (Time-Travel)** qua `CommandManager`. |
| 5 | **Memento** | Behavioral | `vn.edu.dlu.dhopm.pattern.memento` | `DHOListMemento` chụp lại ảnh trạng thái cấu trúc dữ liệu tại từng thời điểm luồng $T_L$ để khôi phục trạng thái hoàn hảo khi Undo/Redo. |
| 6 | **State** | Behavioral | `vn.edu.dlu.dhopm.pattern.state` | Quản lý vòng đời luồng qua máy trạng thái hữu hạn (`IdleState`, `RunningState`, `PausedState`, `CompletedState`) với chuyển trạng thái an toàn. |
| 7 | **Decorator** | Structural | `vn.edu.dlu.dhopm.pattern.decorator` | Mở rộng tính năng miner runtime: `BenchmarkMinerDecorator` (đo microsecond & heap memory), `LoggingMinerDecorator` (ghi vết audit). |
| 8 | **Facade** | Structural | `vn.edu.dlu.dhopm.pattern.facade` | `StreamMiningFacade` cung cấp 1 điểm truy cập duy nhất, che giấu độ phức tạp của 7 subsystem bên dưới. |
| 9 | **Builder** | Creational | `vn.edu.dlu.dhopm.pattern.builder` | `MiningConfig.Builder` thiết lập cấu hình khai phá linh hoạt với fluent API và cơ chế validate tự động. |
| 10 | **Factory Method** | Creational | `vn.edu.dlu.dhopm.pattern.factory` | Đa dạng hóa nguồn dữ liệu: `PaperDatasetFactory`, `SyntheticDatasetFactory`, `DB0DatasetFactory`, `CsvDatasetFactory`. |

---

## 🛠️ 3. Yêu Cầu Môi Trường & Công Nghệ

- **Java Development Kit (JDK):** Java 17 LTS trở lên (Hỗ trợ tốt nhất Java 17, 21 LTS hoặc Java 25).
- **Công cụ build:** Apache Maven 3.8+.
- **Thư viện chính:**
  - JavaFX 21 (Controls, FXML, Graphics).
  - JUnit 5 (JUnit Jupiter) kiểm thử tự động.

---

## 🚀 4. Hướng Dẫn Biên Dịch & Chạy Ứng Dụng

### Khởi chạy bằng Maven Terminal:

```bash
# Di chuyển vào thư mục dự án
cd dhopm-visualizer

# 1. Chạy toàn bộ 41 Unit Tests (100% PASS)
mvn test

# 2. In bảng đối chiếu chi tiết Lab 1 vs Lab 2 ra màn hình Console
mvn compile exec:java -Dexec.mainClass="vn.edu.dlu.dhopm.core.Lab1VerificationRunner"

# 3. Khởi chạy giao diện Design Patterns Studio JavaFX
mvn javafx:run
```

#### 💡 Khởi chạy nhanh bằng script `run.sh`:
```bash
chmod +x run.sh
./run.sh
```

---

## 🎯 5. Tính Năng & Điểm Nhấn Giao Diện Studio

1. **Header Command Center (Cyber Dark Theme):**
   - Đèn LED xung nhịp neon chuyển trạng thái động (`Idle`, `Running`, `Paused`, `Completed`).
   - Hiển thị trực quan 10 huy hiệu GoF Patterns và thông tin dòng thời gian luồng ($T_L$).
2. **Dataset Factory Hub (Factory Method):** Nạp dữ liệu từ bài báo EAAI 2026, luồng ngẫu nhiên Poisson, hoặc file CSV người dùng tải lên.
3. **Mining Strategy Selector (Strategy):** Chuyển đổi linh hoạt giữa các giải thuật suy giảm thời gian và thuật toán cắt tỉa không gian.
4. **Time-Travel Command Dock (Command + Memento):**
   - Hỗ trợ **Play**, **Pause**, **Step Next**, **Undo (Ctrl+Z)**, **Redo (Ctrl+Y)**, **Reset**.
   - Hiển thị độ sâu ngăn xếp Memento thời gian thực.
5. **Decorator Profiler HUD (Decorator):**
   - Thống kê chu kỳ chạy, thời gian thực thi từng batch (microsecond), lượng RAM tiêu thụ đỉnh và tỉ lệ cắt tỉa DUBO.
6. **Card Widgets 3D DHO-List:** Trực quan hóa từng node bằng Card chuyên biệt với thanh tiến trình $d\_sup / min\_sup$, $d\_occ / min\_occ$, và các nhãn trạng thái `DHOP ★`, `Cắt tỉa ✂`, `Mở rộng ➔`.
7. **Tab Studio Mẫu Thiết Kế:**
   - **Template Method Visual Stepper:** Trực quan hóa quy trình 5 bước bất biến.
   - **Memento Stack Inspector:** Quan sát các snapshot trạng thái lưu trữ trong bộ nhớ.
   - **Live Audit Terminal (Observer):** Console hacker ghi nhận toàn bộ sự kiện luồng theo thời gian thực.

---

## 🧪 6. Kết Quả Kiểm Thử (41/41 Unit Tests PASS)

```
[INFO] Results:
[INFO] Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
- **8 Tests:** Xác thực Lab 1 (`Lab1VerificationTest`)
- **10 Tests:** Thuật toán lõi Lab 2 DHOPM (`DHOPMEngineTest`)
- **6 Tests:** Strategy Pattern (`StrategyPatternTest`)
- **5 Tests:** Memento & Command Pattern (`MementoAndCommandPatternTest`)
- **3 Tests:** Decorator Pattern (`DecoratorPatternTest`)
- **3 Tests:** Template Method & Observer Pattern (`TemplateMethodAndObserverPatternTest`)
- **3 Tests:** State & Facade Pattern (`StateAndFacadePatternTest`)
- **2 Tests:** Builder & Factory Pattern (`BuilderAndFactoryPatternTest`)
- **1 Test:** Headless FXML Integration Test (`FXMLInitializationTest`)

---

## 📁 7. Tài Liệu Thiết Kế Kèm Theo
- [Kiến trúc Design Patterns & Phân tích SOLID](docs/DESIGN_PATTERNS_ARCHITECTURE.md)
- [Kịch bản thuyết trình & Vấn đáp 5 phút](docs/DEMO_SCRIPT_5_MINUTES.md)

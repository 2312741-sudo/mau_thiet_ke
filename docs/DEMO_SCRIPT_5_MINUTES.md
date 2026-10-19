# KỊCH BẢN THUYẾT TRÌNH & DEMO ĐỒ ÁN GIỮA KỲ (5 PHÚT)
**Học phần:** Mẫu Thiết Kế (Design Patterns) - Khoa CNTT, Đại Học Đà Lạt  
**Nhóm sinh viên thực hiện:** Võ Công Vinh (2312800) • Nguyễn Đức Tín (2312774) • Ngô Quốc Trường Vĩ (2312796)  
**Đề tài:** Tái cấu trúc Bộ Khai Phá Luồng DHOPM thành **Extensible Stream Mining Framework** với 10 GoF Patterns & Clean Architecture.

---

## 🎯 MỤC TIÊU CỦA BUỔI THUYẾT TRÌNH
1. Chứng minh cho Hội đồng/Giảng viên thấy: **Design Patterns không phải là lý thuyết suông** mà là giải pháp kiến trúc giải quyết triệt để các bài toán kỹ thuật phức tạp (xử lý luồng thời gian thực, hoàn tác trạng thái, đa chiến lược suy giảm).
2. Minh họa trực quan 10 GoF Patterns hoạt động ăn khớp thông qua giao diện JavaFX và bộ kiểm thử 41 unit tests.

---

## ⏱️ PHÂN BỔ THỜI GIAN CHI TIẾT (5 PHÚT)

| Mốc Thời Gian | Nội Dung Trình Bày | Hành Động Demo Trực Quan Trên Màn Hình | Design Patterns Thể Hiện |
| :---: | :--- | :--- | :--- |
| **0:00 - 1:00** | **Đặt vấn đề & Kiến trúc tổng thể**<br>- Giới thiệu bài toán DHOPM (EAAI 2026).<br>- Điểm yếu code cũ (Hardcoded, monolithic).<br>- Giới thiệu kiến trúc Clean Architecture & Facade. | - Khởi chạy ứng dụng bằng lệnh:<br>`mvn javafx:run`<br>- Giới thiệu bố cục giao diện trực quan 4 Tab. | **Facade Pattern**<br>**Builder Pattern**<br>**Factory Method** |
| **1:00 - 2:15** | **Tính năng Đa Chiến Lược (Strategy) & Đo đạc hiệu năng (Decorator)**<br>- Hoán đổi linh hoạt giữa Damped Exponential, Landmark Window và Linear Decay tại runtime.<br>- So sánh DUBO Pruning vs No Pruning. | - Thao tác chọn ComboBox: Đổi từ `Damped Exponential (f=0.9)` sang `Landmark Window (f=1.0)`.<br>- Quan sát kết quả DHOPs nhảy ngay từ **2 mẫu** lên **9 mẫu**.<br>- Chỉ vào ô **"Thời gian chạy (Decorator)"**: Hiển thị chính xác đến **microsecond (µs)** và RAM tiêu thụ. | **Strategy Pattern**<br>**Decorator Pattern**<br>**Template Method** |
| **2:15 - 3:30** | **Tính năng Undo / Step-Back (Command + Memento)**<br>- Điểm sáng kỹ thuật: Cho phép lùi lại giao dịch trước đó.<br>- Chứng minh tính toàn vẹn của DHO-List khi phục hồi từ Snapshot. | - Bấm nút **"➕ Bơm 1 bước"**: Bơm giao dịch T9 vào luồng. TL tăng lên T9, Node A và E tăng support.<br>- Bấm nút **"↩ Undo / Lùi"**: Hệ thống lập tức khôi phục về T8, DHO-List cards và biểu đồ hoàn tác ngay lập tức.<br>- Bơm 3 bước liên tiếp rồi bấm Undo 3 lần liên tiếp. | **Command Pattern**<br>**Memento Pattern** |
| **3:30 - 4:15** | **Quản lý Trạng Thái An Toàn (State) & Nhật ký (Observer)**<br>- Điều khiển trạng thái máy hữu hạn tránh race-condition.<br>- Nhật ký kiểm toán thời gian thực. | - Bấm nút **"▶ Bắt đầu"**: Nút Start và Step tự động Disabled, nút Pause chuyển sang Enabled. Badge trạng thái đổi màu xanh lá.<br>- Bấm **"⏸ Tạm dừng"**: Trạng thái đổi sang màu cam Paused.<br>- Chuyển sang **Tab 4 "Design Patterns & Audit Trail"**: Cho giảng viên xem từng dòng audit log sinh ra từ `AuditLogObserver`. | **State Pattern**<br>**Observer Pattern** |
| **4:15 - 5:00** | **Chứng thực kỹ nghệ phần mềm & Kết luận**<br>- Tuân thủ 5 nguyên lý SOLID.<br>- Bật terminal chạy bộ kiểm thử tự động. | - Chạy lệnh `mvn test` trên Terminal.<br>- Cho thấy **41/41 test cases PASS 100%** (18 test gốc bài báo + 23 test GoF Patterns) trong 1.3 giây.<br>- Lời cảm ơn và mời Giảng viên phản biện. | **SOLID Principles**<br>**Clean Architecture** |

---

## 🎙️ LỜI THOẠI MẪU CHO TỪNG PHÚT (SPEAKER SCRIPT)

### Phút 0:00 - 1:00: Mở đầu & Kiến trúc
> *"Kính thưa Thầy/Cô trong Hội đồng, nhóm em gồm các thành viên: Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796).  
> Trong đồ án giữa kỳ này, em đã tái cấu trúc thuật toán khoa học chất lượng cao **DHOPM (EAAI 2026)** thành một **Extensible Stream Mining Framework** áp dụng trọn vẹn **10 Mẫu Thiết Kế GoF**.  
> Thay vì để giao diện JavaFX gắn chặt với thuật toán, em xây dựng lớp **`StreamMiningFacade`** làm điểm truy cập duy nhất, áp dụng **Builder Pattern** cho `MiningConfig` và **Factory Method** để nạp dữ liệu đa nguồn. Toàn bộ mã nguồn tuân thủ triệt để nguyên lý SOLID."*

### Phút 1:00 - 2:15: Strategy Pattern & Decorator Pattern
> *"Thứ nhất, về **Strategy Pattern**: Thuật toán stream thường yêu cầu thử nghiệm nhiều hàm suy giảm thời gian. Em đã trừu tượng hóa thành interface `DecayStrategy`.  
> Xin Thầy/Cô quan sát trên màn hình: Khi em chọn `Damped Exponential f=0.9`, hệ thống tìm được đúng **2 mẫu DHOP** ({AE, F}) khớp số liệu bài báo.  
> Ngay khi em hoán đổi sang `Landmark Window (f=1.0)` tại ComboBox này, không cần khởi động lại, thuật toán tự tái cấu trúc và tìm ra **9 mẫu DHOP**!  
> Thứ hai, về **Decorator Pattern**: Làm sao để đo thời gian chạy nanosecond và RAM mà không chèn code rác vào thuật toán? Em xây dựng `BenchmarkMinerDecorator` bọc ngoài miner. Thầy/Cô có thể thấy thời gian thực thi hiển thị chính xác đến **microsecond** (ví dụ: 215.4 µs) ngay trên nhãn thống kê này."*

### Phút 2:15 - 3:30: Điểm sáng Command Pattern & Memento Pattern
> *"Thứ ba, đây là tính năng đột phá nhất của đồ án: **Undo / Step-Back trên luồng dữ liệu thời gian thực**.  
> Trong khai phá dữ liệu luồng, việc quan sát cấu trúc DHO-List biến đổi khi có một giao dịch mới đến là rất khó nếu không thể tua lại.  
> Bằng cách kết hợp **Command Pattern** (`StepNextCommand`) với **Memento Pattern** (`DHOListMemento`), mỗi khi em bấm **'➕ Bơm 1 bước'**, hệ thống tự động chụp một bản sao sâu (Deep snapshot) của DHO-List trước khi nạp giao dịch T9.  
> Và khi em bấm **'↩ Undo / Lùi'**, DHOList lập tức phục hồi nguyên trạng về T8! Mọi node, thẻ màu, biểu đồ DO và bảng kết quả đều quay về đúng thời điểm trước đó. Hệ thống hỗ trợ hoàn tác liên tiếp nhiều bước nhờ ngăn xếp trong `CommandManager`."*

### Phút 3:30 - 4:15: State Pattern & Observer Pattern
> *"Thứ tư, về **State Pattern**: Để đảm bảo an toàn tương tranh giữa luồng Timer ngầm và giao diện, em mô hình hóa hệ thống thành máy trạng thái 4 pha: `IdleState`, `RunningState`, `PausedState`, và `CompletedState`.  
> Khi em bấm Start, trạng thái chuyển sang Running, các nút Start và Step tự động bị khóa, chỉ cho phép Pause. Khi hết dữ liệu, nó tự chuyển sang Completed. Điều này loại bỏ hoàn toàn các cờ boolean và rẽ nhánh phức tạp.  
> Thứ năm, về **Observer Pattern**: Tại **Tab 4 (Design Patterns & Audit Trail)**, toàn bộ sự kiện lô giao dịch đến và kết quả khai phá được truyền bất đồng bộ đến `AuditLogObserver` và `BenchmarkMetricsObserver` để lập nhật ký kiểm toán."*

### Phút 4:15 - 5:00: Chứng thực SOLID & Kết luận
> *"Cuối cùng, kiến trúc của em đạt tính đóng gói tuyệt đối (OCP): Thầy/Cô muốn thêm giải thuật suy giảm mới (như Sigmoid Decay) hay nguồn dữ liệu mới chỉ cần thêm 1 file class mới mà không chạm vào code cũ.  
> Để chứng minh tính đúng đắn 100%, em xin phép chạy bộ kiểm thử JUnit 5 trên Terminal:  
> `mvn test`  
> Toàn bộ **41 test cases đều PASS tuyệt đối**, bao gồm 18 test toán học từ Lab 1 và 23 test chuyên biệt cho từng Design Pattern chỉ trong vòng **1.3 giây**!  
> Em xin chân thành cảm ơn Quý Thầy/Cô đã lắng nghe và rất mong nhận được những góp ý quý báu ạ!"*

---

## 💡 CÁC CÂU HỎI HỘI ĐỒNG THƯỜNG HỎI & CÁCH TRẢ LỜI ĐẠT ĐIỂM TỐI ĐA

### Q1: Tại sao không dùng Listener có sẵn của JavaFX mà phải tự cài Observer Pattern?
> **Trả lời:** *"Dạ thưa Thầy/Cô, JavaFX Property Listener chỉ ràng buộc được các kiểu dữ liệu UI trên luồng đồ họa (JavaFX Application Thread). Còn `StreamObserver` và `MiningObserver` trong framework của em là các interface nghiệp vụ thuần túy (Domain-level Observers), hoạt động độc lập ở tầng core, cho phép đăng ký các bộ ghi log (AuditLogObserver), xuất console, hoặc gửi dữ liệu qua WebSocket/Kafka mà hoàn toàn không phụ thuộc vào thư viện JavaFX."*

### Q2: Memento Pattern lưu snapshot của DHO-List có gây tốn RAM không khi luồng dữ liệu lớn?
> **Trả lời:** *"Dạ thưa Thầy/Cô, DHO-List là cấu trúc tóm lược nén (One-scan summary), mỗi Node chỉ lưu các bản ghi `Entry(tid, tlen)` là Java Record cực kỳ nhẹ. Hơn nữa, `CommandManager` của em được cài đặt giới hạn lịch sử hoàn tác (Circular History Window, mặc định tối đa 50 bước gần nhất), khi vượt quá sẽ tự động dọn dẹp các memento cũ nhất, đảm bảo mức tiêu thụ RAM luôn ở ngưỡng an toàn dưới 10MB."*

### Q3: Em thể hiện nguyên lý Dependency Inversion Principle (DIP) ở đâu?
> **Trả lời:** *"Dạ, tầng cao là `StreamMiningFacade` và `AbstractStreamMiner` không hề phụ thuộc trực tiếp vào các lớp cụ thể như `DampedExponentialDecay` hay `DUBOPruningStrategy`. Thay vào đó, chúng phụ thuộc vào các abstraction là `DecayStrategy` và `PruningStrategy`. Các implementation cụ thể được inject thông qua `MiningConfig.Builder` tại thời điểm khởi tạo."*

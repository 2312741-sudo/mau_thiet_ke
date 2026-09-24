package vn.edu.dlu.dhopm.pattern.command;

/**
 * Command Interface trong <b>Command Pattern (GoF)</b>.
 *
 * <p>Đóng gói toàn bộ thông tin của một yêu cầu thao tác trên luồng dữ liệu thành một đối tượng độc lập.
 * Cho phép tham số hóa các yêu cầu, xếp hàng thực thi (Queuing), ghi nhật ký hoạt động
 * và đặc biệt là hỗ trợ khả năng hoàn tác (Undo / Redo) kết hợp cùng Memento Pattern.</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public interface StreamCommand {

    /**
     * Thực thi lệnh thao tác trên luồng.
     */
    void execute();

    /**
     * Hoàn tác lại thao tác vừa thực hiện (Undo), khôi phục trạng thái trước đó.
     */
    void undo();

    /**
     * Mô tả hành động phục vụ hiển thị lịch sử trên giao diện.
     */
    String getDescription();

    /**
     * Kiểm tra xem lệnh này có hỗ trợ hoàn tác hay không.
     */
    default boolean canUndo() {
        return true;
    }
}

package vn.edu.dlu.dhopm.pattern.strategy;

/**
 * Strategy Interface định nghĩa chiến lược cắt tỉa không gian tìm kiếm (Pruning Strategy).
 *
 * <p>Áp dụng <b>Strategy Pattern (GoF)</b>:
 * Cho phép linh hoạt lựa chọn hoặc hoán đổi giữa việc sử dụng cận trên chặt DUBO
 * và việc duyệt toàn bộ không gian (không cắt tỉa) để đo lường hiệu năng thực tế.</p>
 *
 * @author Nguyen Thanh Tam - MSSV: 2312741
 */
public interface PruningStrategy {

    /**
     * Xác định xem nhánh tìm kiếm của mẫu X có bị cắt tỉa hay không.
     *
     * @param upperBound Giá trị cận trên độ chiếm dụng (DUBO)
     * @param minSup     Ngưỡng hỗ trợ tối thiểu
     * @return true nếu nhánh cần bị cắt tỉa; false nếu tiếp tục mở rộng DFS
     */
    boolean shouldPrune(double upperBound, double minSup);

    /**
     * Tên hiển thị của chiến lược cắt tỉa.
     */
    String getName();

    /**
     * Mô tả chiến lược cắt tỉa.
     */
    String getDescription();
}

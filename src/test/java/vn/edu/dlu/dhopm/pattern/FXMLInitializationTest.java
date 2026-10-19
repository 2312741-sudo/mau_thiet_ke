package vn.edu.dlu.dhopm.pattern;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.dlu.dhopm.ui.controller.MainController;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tự động tính hợp lệ của giao diện FXML và Controller JavaFX.
 *
 * @author Võ Công Vinh (2312800), Nguyễn Đức Tín (2312774), Ngô Quốc Trường Vĩ (2312796)
 */
public class FXMLInitializationTest {

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        // Cấu hình JavaFX chạy chế độ headless trên môi trường kiểm thử CI/CD
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX Toolkit phải khởi động thành công");
    }

    @Test
    @DisplayName("UI - FXML & Controller: Khởi tạo FXML và liên kết các thành phần điều khiển thành công")
    void testFXMLLoadingAndControllerBinding() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<MainController> controllerRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                URL fxmlUrl = getClass().getResource("/vn/edu/dlu/dhopm/view/main.fxml");
                assertNotNull(fxmlUrl, "Không tìm thấy file main.fxml trong classpath");

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                assertNotNull(root, "Parent node không được null sau khi load");

                MainController controller = loader.getController();
                assertNotNull(controller, "MainController phải được gắn kết thành công");
                controllerRef.set(controller);
            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Quá trình load FXML bị timeout");
        if (errorRef.get() != null) {
            fail("Lỗi khi load FXML: " + errorRef.get().getMessage(), errorRef.get());
        }
        assertNotNull(controllerRef.get());
    }
}

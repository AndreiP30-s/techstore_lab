package com.example.techstore.console;

import com.example.techstore.model.product.FrontCamera;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import com.example.techstore.model.product.*;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class ProductInputTest {
    private Product read(String script) {
        return new ProductInput(new ConsoleIO(new StringReader(script), new PrintWriter(new StringWriter())), () -> 100001L).read();
    }

    // Ввод смартфона с несколькими задними камерами.
    @Test
    void readsSmartphoneWithMultipleRearCameras() {
        Smartphone phone = (Smartphone) read("1\nPhone\nBrand\n100\nCPU\n8\n256\n6,2\n4000\nда\n12\n2\n1\n50\n2\n12\n");
        assertEquals(6.2, phone.getScreenDiagonalInches());
        assertTrue(phone.supports5g());
        assertEquals(2, phone.getCameras().getRearCameraCount());
        assertEquals(CameraType.ULTRA_WIDE, phone.getCameras().rearCameras().get(1).type());
    }

    // Ввод планшета с исправлением неверной диагонали.
    @Test
    void readsTabletAndRejectsNonFiniteDiagonal() {
        Tablet tablet = (Tablet) read("3\nTab\nBrand\n100\nCPU\n8\n128\nNaN\nInfinity\n11\n8000\nнет\n12\n1\n1\n13\n");
        assertEquals(11.0, tablet.getScreenDiagonalInches());
        assertFalse(tablet.isCellular());
        assertEquals(13.0, tablet.getCameras().rearCameras().get(0).megapixels());
    }

    // Ввод ноутбука с SSD и веб-камерой.
    @Test
    void readsLaptopWithWebcamAndSsd() {
        Laptop laptop = (Laptop) read("2\nBook\nBrand\n100\nCPU\n16\n512\n14\n1\n2\n");
        assertEquals(StorageType.SSD, laptop.getStorageType());
        assertEquals(2.0, laptop.getFrontCamera().megapixels());
        assertEquals(16, laptop.getRamGb());
    }
    // Артикул выдаётся без ввода пользователем.
    @Test
    void generatesArticleWithoutAskingUser() {
        StringWriter output = new StringWriter();
        ConsoleIO io = new ConsoleIO(new StringReader("4\nBuds\nBrand\n100\n1\nда\nнет\n20\n20000\n"),
                new PrintWriter(output));
        Product product = new ProductInput(io, () -> 100005L).read();
        assertEquals(100005L, product.getArticle());
        assertFalse(output.toString().contains("Артикул"));
    }

    // Отмена формы не расходует артикул.
    @Test
    void cancelledFormDoesNotAllocateArticle() {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        ConsoleIO io = new ConsoleIO(new StringReader("4\n/cancel\n"), new PrintWriter(new StringWriter()));
        ProductInput input = new ProductInput(io, () -> { calls.incrementAndGet(); return 100001L; });
        assertThrows(InputCancelledException.class, input::read);
        assertEquals(0, calls.get());
    }
}

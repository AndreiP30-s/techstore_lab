package com.example.techstore;

import com.example.techstore.model.product.FrontCamera;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.example.techstore.model.product.Camera;
import com.example.techstore.model.product.CameraSetup;
import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.Headphones;
import com.example.techstore.model.product.HeadphoneType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CameraAndAudioTest {
    // Количество, типы и разрешения камер.
    @Test
    void countsAndDisplaysEachCameraWithItsOwnResolution() {
        FrontCamera front = new FrontCamera(12.0);
        CameraSetup setup = new CameraSetup(front, List.of(
                new Camera(CameraType.WIDE, 50.0),
                new Camera(CameraType.ULTRA_WIDE, 12.0),
                new Camera(CameraType.TELEPHOTO, 10.0)));
        assertEquals(front, setup.frontCamera());
        assertEquals(3, setup.getRearCameraCount());
        assertEquals(4, setup.getTotalCameraCount());
        String text = setup.getSpecifications();
        for (String part : List.of("Фронтальная камера (1)", "задние камеры (3)",
                "широкоугольная 50.0 Мп", "сверхширокоугольная 12.0 Мп", "телефото 10.0 Мп", "всего камер: 4")) {
            assertTrue(text.contains(part), part);
        }
    }

    // Список камер нельзя изменить через исходный или возвращённый список.
    @Test
    void cameraListIsAnImmutableSnapshot() {
        Camera camera = new Camera(CameraType.WIDE, 12.0);
        List<Camera> source = new ArrayList<>(List.of(camera));
        CameraSetup setup = new CameraSetup(new FrontCamera(12.0), source);
        source.clear();
        assertEquals(1, setup.getRearCameraCount());
        assertThrows(UnsupportedOperationException.class, () -> setup.rearCameras().clear());
    }

    // Недопустимые значения разрешения камеры.
    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void rejectsInvalidResolution(double resolution) {
        assertThrows(IllegalArgumentException.class, () -> new Camera(CameraType.WIDE, resolution));
        assertThrows(IllegalArgumentException.class, () -> new FrontCamera(resolution));
    }

    // Отсутствующие камеры и типы отклоняются.
    @Test
    void rejectsMissingCamerasAndTypes() {
        Camera camera = new Camera(CameraType.WIDE, 12.0);
        assertThrows(NullPointerException.class, () -> new Camera(null, 12.0));
        assertThrows(NullPointerException.class, () -> new CameraSetup(null, List.of(camera)));
        assertThrows(NullPointerException.class, () -> new CameraSetup(new FrontCamera(12.0), null));
        assertThrows(IllegalArgumentException.class, () -> new CameraSetup(new FrontCamera(12.0), List.of()));
        List<Camera> containsNull = new ArrayList<>();
        containsNull.add(null);
        assertThrows(NullPointerException.class, () -> new CameraSetup(new FrontCamera(12.0), containsNull));
    }

    private Headphones headphones(int min, int max) {
        return new Headphones(TestArticles.next(), "Sound", "DemoAudio", BigDecimal.TEN,
                HeadphoneType.OVER_EAR, true, true, min, max);
    }

    // Частоты сохраняются и выводятся в герцах.
    @Test
    void storesAndDisplaysAudioRangeInHertz() {
        Headphones headphones = headphones(20, 20000);
        assertEquals(20, headphones.getMinFrequencyHz());
        assertEquals(20000, headphones.getMaxFrequencyHz());
        assertTrue(headphones.getSpecifications().contains("20–20000 Гц"));
    }

    // Неверные границы частот отклоняются.
    @Test
    void rejectsInvalidAudioRange() {
        assertThrows(IllegalArgumentException.class, () -> headphones(0, 20000));
        assertThrows(IllegalArgumentException.class, () -> headphones(-10, 20000));
        assertThrows(IllegalArgumentException.class, () -> headphones(20, 0));
        assertThrows(IllegalArgumentException.class, () -> headphones(20, 20));
        assertThrows(IllegalArgumentException.class, () -> headphones(20000, 20));
    }
}

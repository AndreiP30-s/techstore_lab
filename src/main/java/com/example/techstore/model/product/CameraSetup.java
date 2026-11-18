package com.example.techstore.model.product;

import java.util.List;
import java.util.Objects;

/** Одна фронтальная и непустой список задних камер смартфона или планшета. */
public record CameraSetup(FrontCamera frontCamera, List<Camera> rearCameras) {
    public CameraSetup {
        Objects.requireNonNull(frontCamera, "frontCamera");
        rearCameras = List.copyOf(rearCameras);
        if (rearCameras.isEmpty()) {
            throw new IllegalArgumentException("At least one rear camera is required");
        }
    }

    public int getRearCameraCount() { return rearCameras.size(); }
    public int getTotalCameraCount() { return 1 + rearCameras.size(); }

    public String getSpecifications() {
        StringBuilder rear = new StringBuilder();
        for (Camera camera : rearCameras) {
            if (rear.length() > 0) { rear.append("; "); }
            rear.append(camera.getSpecifications());
        }
        return "Фронтальная камера (1): " + frontCamera.getSpecifications()
                + ", задние камеры (" + getRearCameraCount() + "): " + rear
                + ", всего камер: " + getTotalCameraCount();
    }
}

package com.example.recocnocimientopostural;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.List;

@ExperimentalGetImage
public class PoseDetectionHelper implements ImageAnalysis.Analyzer {

    public interface GestureCallback {
        void onSmileDetected();
        void onEyesClosedDetected();
        void onTongueOutDetected();
    }

    private final GestureCallback callback;
    private final FaceDetector detector;

    public PoseDetectionHelper(GestureCallback callback) {
        this.callback = callback;

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        detector = FaceDetection.getClient(options);
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        detector.process(image)
                .addOnSuccessListener(faces -> detectGestures(faces))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void detectGestures(List<Face> faces) {
        for (Face face : faces) {
            // 1️⃣ Ojos cerrados
            if (face.getLeftEyeOpenProbability() != null && face.getRightEyeOpenProbability() != null) {
                float left = face.getLeftEyeOpenProbability();
                float right = face.getRightEyeOpenProbability();
                if (left < 0.4 && right < 0.4) {
                    callback.onEyesClosedDetected();
                    return;
                }
            }

            // 2️⃣ Sonrisa
            if (face.getSmilingProbability() != null) {
                float smile = face.getSmilingProbability();
                if (smile > 0.7) {
                    callback.onSmileDetected();
                    return;
                }
            }

            // 3️⃣ Lengua afuera / boca abierta
            FaceLandmark mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT);
            FaceLandmark mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
            FaceLandmark noseBase = face.getLandmark(FaceLandmark.NOSE_BASE);

            if (mouthLeft != null && mouthRight != null && noseBase != null) {
                float mouthWidth = mouthRight.getPosition().x - mouthLeft.getPosition().x;
                float mouthHeight = Math.abs(noseBase.getPosition().y - ((mouthLeft.getPosition().y + mouthRight.getPosition().y) / 2));
                if (mouthHeight / mouthWidth > 0.6f) { // Ajusta el umbral según tu prueba
                    callback.onTongueOutDetected();
                    return;
                }
            }
        }
    }

    public void close() {
        detector.close();
    }
}

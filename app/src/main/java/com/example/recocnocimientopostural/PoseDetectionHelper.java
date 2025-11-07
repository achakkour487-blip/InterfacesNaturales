package com.example.recocnocimientopostural;


import android.media.Image; // Importar la clase Image
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
// ❗️ IMPORTAR ESTO


import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.google.mlkit.vision.pose.PoseLandmark;


@OptIn(markerClass = ExperimentalGetImage.class) // ❗️ AÑADIR ESTA LÍNEA ENCIMA DE LA CLASE
public class PoseDetectionHelper implements ImageAnalysis.Analyzer {


    public interface GestureCallback {
        void onHeadTiltLeft();
        void onHeadTiltRight();
        void onShoulderRaise();
    }


    private final GestureCallback callback;
    private final PoseDetector detector;


    public PoseDetectionHelper(GestureCallback callback) {
        this.callback = callback;
        AccuratePoseDetectorOptions options =
                new AccuratePoseDetectorOptions.Builder()
                        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                        .build();
        detector = PoseDetection.getClient(options);
    }


    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage(); // Obtener la imagen una vez


        if (mediaImage == null) {
            imageProxy.close();
            return;
        }


        InputImage image = InputImage.fromMediaImage(
                mediaImage, // Usar la variable
                imageProxy.getImageInfo().getRotationDegrees()
        );


        detector.process(image)
                .addOnSuccessListener(this::detectGestures)
                .addOnCompleteListener(task -> imageProxy.close());
    }


    private void detectGestures(Pose pose) {
        if (pose.getAllPoseLandmarks().isEmpty()) return;


        float headX = pose.getPoseLandmark(PoseLandmark.NOSE).getPosition().x;
        float shoulderLeftX = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().x;
        float shoulderRightX = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().x;
        float shoulderLeftY = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().y;
        float shoulderRightY = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().y;


        if (headX < shoulderLeftX - 30) callback.onHeadTiltLeft();
        if (headX > shoulderRightX + 30) callback.onHeadTiltRight();
        if (shoulderRightY < shoulderLeftY - 40) callback.onShoulderRaise();
    }
}

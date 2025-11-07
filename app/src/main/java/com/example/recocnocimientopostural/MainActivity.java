package com.example.recocnocimientopostural;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.recocnocimientopostural.R;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import io.github.sceneview.SceneView;

public class MainActivity extends AppCompatActivity
        implements VoiceController.VoiceCallback, PoseDetectionHelper.GestureCallback {

    private VoiceController voiceController;
    private ARSceneManager arSceneManager;
    private PoseDetectionHelper poseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        PreviewView cameraView = findViewById(R.id.cameraView);
        SceneView sceneView = findViewById(R.id.sceneView);
        Button btnVoice = findViewById(R.id.btnVoice);

        voiceController = new VoiceController(this, this);
        arSceneManager = new ARSceneManager(sceneView, getLifecycle());
        poseHelper = new PoseDetectionHelper(this);

        btnVoice.setOnClickListener(v -> voiceController.startListening());

        if (hasPermissions()) startCamera(cameraView);
        else requestPermissions();
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 10);
    }

    private void startCamera(PreviewView previewView) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), poseHelper);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ==========================
    // VOICE CALLBACK
    // ==========================
    @Override
    public void onCommandRecognized(String command) {
        if (command.contains("luz")) {
            arSceneManager.highlight("luz");
            voiceController.speak("Luz encendida");
        } else if (command.contains("temperatura") || command.contains("calefacción")) {
            arSceneManager.highlight("temperatura");
            voiceController.speak("Temperatura ajustada");
        } else if (command.contains("puerta")) {
            arSceneManager.highlight("puerta");
            voiceController.speak("Puerta abierta");
        } else {
            voiceController.speak("No entendí el comando");
        }
    }

    // ==========================
    // GESTURE CALLBACK
    // ==========================
    @Override
    public void onHeadTiltLeft() { voiceController.speak("Acción cancelada"); }

    @Override
    public void onHeadTiltRight() { voiceController.speak("Confirmado"); }

    @Override
    public void onShoulderRaise() { voiceController.speak("Siguiente opción"); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        voiceController.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                PreviewView cameraView = findViewById(R.id.cameraView);
                startCamera(cameraView);
            }
        }
    }
}

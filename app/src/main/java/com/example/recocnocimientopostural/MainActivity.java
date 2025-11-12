package com.example.recocnocimientopostural;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import io.github.sceneview.SceneView;

@ExperimentalGetImage
public class MainActivity extends AppCompatActivity
        implements VoiceController.VoiceCallback, PoseDetectionHelper.GestureCallback {

    private VoiceController voiceController;
    private ARSceneManager arSceneManager;
    private PoseDetectionHelper poseHelper;

    private long lastGestureTime = 0;
    private static final long DEBOUNCE_INTERVAL = 800;

    private Button btnPuerta, btnLuz, btnTemperatura;
    private boolean mandoVisible = false;
    private int selectedButtonIndex = 0;
    private Button[] mandoButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreviewView cameraView = findViewById(R.id.cameraView);
        SceneView sceneView = findViewById(R.id.sceneView);

        btnPuerta = findViewById(R.id.btnPuerta);
        btnLuz = findViewById(R.id.btnLuz);
        btnTemperatura = findViewById(R.id.btnTemperatura);
        Button btnVoice = findViewById(R.id.btnVoice);

        mandoButtons = new Button[]{btnPuerta, btnLuz, btnTemperatura};
        hideMando();

        voiceController = new VoiceController(this, this);
        arSceneManager = new ARSceneManager(sceneView, getLifecycle());
        poseHelper = new PoseDetectionHelper(this);

        btnPuerta.setOnClickListener(v -> askDoorState());
        btnLuz.setOnClickListener(v -> askLightState());
        btnTemperatura.setOnClickListener(v -> askTemperature());
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

    private void askLightState() {
        arSceneManager.highlight("luz");
        voiceController.speak("¿Quieres encender o apagar la luz?");
        voiceController.setNextCommandCallback(cmd -> {
            cmd = cmd.toLowerCase();
            if (cmd.contains("encender")) {
                arSceneManager.highlight("luz");
                voiceController.speak("Luz encendida");
            } else if (cmd.contains("apagar")) {
                arSceneManager.unhighlight();
                voiceController.speak("Luz apagada");
            } else {
                voiceController.speak("No entendí el comando");
            }
        });
        voiceController.startListening();
    }

    private void askTemperature() {
        arSceneManager.highlight("temperatura");
        voiceController.speak("¿Qué temperatura quieres poner?");
        voiceController.setNextCommandCallback(cmd -> {
            try {
                int temp = Integer.parseInt(cmd.replaceAll("[^0-9]", ""));
                voiceController.speak("Temperatura ajustada a " + temp + " grados");
            } catch (NumberFormatException e) {
                voiceController.speak("No entendí la temperatura");
            }
        });
        voiceController.startListening();
    }

    private void askDoorState() {
        arSceneManager.highlight("puerta");
        voiceController.speak("¿Quieres abrir o cerrar la puerta?");
        voiceController.setNextCommandCallback(cmd -> {
            cmd = cmd.toLowerCase();
            if (cmd.contains("abrir")) {
                arSceneManager.highlightDoor(true);
                voiceController.speak("Puerta abierta");
            } else if (cmd.contains("cerrar")) {
                arSceneManager.highlightDoor(false);
                voiceController.speak("Puerta cerrada");
            } else {
                voiceController.speak("No entendí el comando");
            }
        });
        voiceController.startListening();
    }

    private void showMando() {
        mandoVisible = true;
        for (Button b : mandoButtons) b.setVisibility(View.VISIBLE);
        selectedButtonIndex = 0;
        highlightSelectedButton();
    }

    private void hideMando() {
        mandoVisible = false;
        for (Button b : mandoButtons) b.setVisibility(View.GONE);
    }

    private void highlightSelectedButton() {
        for (int i = 0; i < mandoButtons.length; i++) {
            mandoButtons[i].setAlpha(i == selectedButtonIndex ? 1f : 0.5f);
        }
    }

    private void selectNextButton() {
        selectedButtonIndex = (selectedButtonIndex + 1) % mandoButtons.length;
        highlightSelectedButton();
    }

    private void selectPreviousButton() {
        selectedButtonIndex = (selectedButtonIndex - 1 + mandoButtons.length) % mandoButtons.length;
        highlightSelectedButton();
    }

    private void activateSelectedButton() {
        mandoButtons[selectedButtonIndex].performClick();
        hideMando();
    }

    @Override
    public void onCommandRecognized(String command) {
        command = command.toLowerCase();
        if (command.contains("luz")) askLightState();
        else if (command.contains("puerta")) askDoorState();
        else if (command.contains("temperatura") || command.contains("calefacción")) askTemperature();
        else if (command.contains("mando")) showMando();
        else voiceController.speak("No entendí el comando");
    }


    public void onHeadTiltLeft() {
        if (!mandoVisible) return;
        if (System.currentTimeMillis() - lastGestureTime > DEBOUNCE_INTERVAL) {
            selectPreviousButton();
            lastGestureTime = System.currentTimeMillis();
        }
    }


    public void onHeadTiltRight() {
        if (!mandoVisible) return;
        if (System.currentTimeMillis() - lastGestureTime > DEBOUNCE_INTERVAL) {
            selectNextButton();
            lastGestureTime = System.currentTimeMillis();
        }
    }


    public void onShoulderRaise() {
        if (!mandoVisible) return;
        if (System.currentTimeMillis() - lastGestureTime > DEBOUNCE_INTERVAL) {
            activateSelectedButton();
            lastGestureTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onSmileDetected() {
        askDoorState();
    }

    @Override
    public void onEyesClosedDetected() {
        askLightState();
    }

    @Override
    public void onTongueOutDetected() {
        askTemperature();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        voiceController.destroy();
        poseHelper.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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

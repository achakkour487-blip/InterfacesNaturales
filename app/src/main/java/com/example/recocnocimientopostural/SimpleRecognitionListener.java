package com.example.recocnocimientopostural;


import android.speech.RecognitionListener;


public abstract class SimpleRecognitionListener implements RecognitionListener {
    @Override public void onReadyForSpeech(android.os.Bundle params) {}
    @Override public void onBeginningOfSpeech() {}
    @Override public void onRmsChanged(float rmsdB) {}
    @Override public void onBufferReceived(byte[] buffer) {}
    @Override public void onEndOfSpeech() {}
    @Override public void onError(int error) {}
    @Override public void onPartialResults(android.os.Bundle partialResults) {}
    @Override public void onEvent(int eventType, android.os.Bundle params) {}
}

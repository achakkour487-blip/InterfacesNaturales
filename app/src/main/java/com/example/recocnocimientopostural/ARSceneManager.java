package com.example.recocnocimientopostural;

import android.graphics.Color;

import dev.romainguy.kotlin.math.Float3;
import io.github.sceneview.SceneView;
import io.github.sceneview.node.Node;
import androidx.lifecycle.Lifecycle;

public class ARSceneManager {

    private final SceneView sceneView;
    private final Lifecycle lifecycle;

    private Node luzNode;
    private Node tempNode;
    private Node puertaNode;

    public ARSceneManager(SceneView sceneView, Lifecycle lifecycle) {
        this.sceneView = sceneView;
        this.lifecycle = lifecycle;
        setupScene();
    }

    private void setupScene() {
        // --- Nodo luz ---
        luzNode = new Node(sceneView.getEngine(), 1);
        luzNode.setPosition(new Float3(0f, 0f, -1f));
        luzNode.setScale(0.2f);

        // --- Nodo temperatura ---
        tempNode = new Node(sceneView.getEngine(), 2);
        tempNode.setPosition(new Float3(0.4f, 0f, -1f));
        tempNode.setScale(0.2f);

        // --- Nodo puerta ---
        puertaNode = new Node(sceneView.getEngine(), 3);
        puertaNode.setPosition(new Float3(-0.4f, 0f, -1f));
        puertaNode.setScale(0.2f);

        // Agregar nodos a la escena
        sceneView.addChildNode(luzNode);
        sceneView.addChildNode(tempNode);
        sceneView.addChildNode(puertaNode);
    }

    public void highlight(String target) {
        int color;
        switch (target) {
            case "luz":
                color = Color.YELLOW;
                break;
            case "temperatura":
                color = Color.RED;
                break;
            case "puerta":
                color = Color.GREEN;
                break;
            default:
                color = Color.GRAY;
        }

        sceneView.setBackgroundColor(color);
    }
}

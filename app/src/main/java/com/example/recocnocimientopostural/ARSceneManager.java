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

    private final int defaultColor = Color.BLACK; // color de fondo por defecto

    public ARSceneManager(SceneView sceneView, Lifecycle lifecycle) {
        this.sceneView = sceneView;
        this.lifecycle = lifecycle;
        setupScene();
    }

    private void setupScene() {
        luzNode = new Node(sceneView.getEngine(), 1);
        luzNode.setPosition(new Float3(0f, 0f, -1f));
        luzNode.setScale(0.2f);

        tempNode = new Node(sceneView.getEngine(), 2);
        tempNode.setPosition(new Float3(0.4f, 0f, -1f));
        tempNode.setScale(0.2f);

        puertaNode = new Node(sceneView.getEngine(), 3);
        puertaNode.setPosition(new Float3(-0.4f, 0f, -1f));
        puertaNode.setScale(0.2f);

        sceneView.addChildNode(luzNode);
        sceneView.addChildNode(tempNode);
        sceneView.addChildNode(puertaNode);
    }

    // Resalta un target cambiando el color de fondo
    public void highlight(String target) {
        int color;
        switch (target) {
            case "luz":
                color = Color.YELLOW;
                break;
            case "temperatura":
                color = Color.MAGENTA;
                break;
            case "puerta":
                color = Color.BLUE;
                break;
            default:
                color = Color.GRAY;
        }
        sceneView.setBackgroundColor(color);
    }

    // “Apaga” cualquier highlight, vuelve al fondo por defecto
    public void unhighlight() {
        sceneView.setBackgroundColor(defaultColor);
    }

    // Resalta la puerta abierta o cerrada
    public void highlightDoor(boolean abierta) {
        sceneView.setBackgroundColor(abierta ? Color.GREEN : Color.RED);
    }
}

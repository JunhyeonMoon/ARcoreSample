package com.example.paranocs.arcoresample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Sphere;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ArActivity extends AppCompatActivity {
    private String TAG = getClass().getName();
    private Context context;

    private ArFragment arFragment;
    private ModelRenderable modelRenderable;

    private Button button_ball;
    private Button button_line;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        context = getApplicationContext();

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        button_ball = findViewById(R.id.button_ball);
        button_line = findViewById(R.id.button_line);

        setButtonBallAction();
        setButtonLineAction();


    }

    private void setButtonBallAction(){
        button_ball.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeBall();
            }
        });
    }
    private void setButtonLineAction(){
        button_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeLine();
            }
        });
    }

    private void makeBall(){
        ArSceneView arSceneView = arFragment.getArSceneView();
        Scene scene = arSceneView.getScene();

        Vector3 dirVec = scene.getCamera().getForward();
        Vector3 cameraPos = scene.getCamera().getWorldPosition();
        Vector3 targetPos = Vector3.add(dirVec, cameraPos);


        Renderable[] ballRenderable = new Renderable[1];

        MaterialFactory.makeOpaqueWithColor(context, new Color(android.graphics.Color.RED))
                .thenAccept(
                    material -> {
                        ballRenderable[0] = ShapeFactory.makeSphere(0.5f, targetPos, material);
                    });

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setWorldPosition(targetPos);
        node.setParent(scene);

        Node ball_node = new Node();
        ball_node.setParent(node);
        ball_node.setRenderable(ballRenderable[0]);

    }
    private void makeLine(){
        //TODO make line
    }

}

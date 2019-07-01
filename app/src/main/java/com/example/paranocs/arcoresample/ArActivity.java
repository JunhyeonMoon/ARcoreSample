package com.example.paranocs.arcoresample;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Sphere;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ArActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private String TAG = getClass().getName();
    private Context context;

    private ArFragment arFragment;

    private PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
    private DisplayRotationHelper displayRotationHelper;
    private GLSurfaceView surfaceView;

    private Button button_ball;
    private Button button_line;
    private Button button_pointcloud;

    private boolean lineActive = false;
    private boolean isClickedSecond = false;
    private Vector3 previousPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        context = getApplicationContext();

        displayRotationHelper = new DisplayRotationHelper(context);
        surfaceView = findViewById(R.id.surfaceView);

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        button_ball = findViewById(R.id.button_ball);
        button_line = findViewById(R.id.button_line);
        button_pointcloud = findViewById(R.id.button_pointcloud);

        setButtonBallAction();
        setButtonLineAction();
        setButtonPointCloudAction();
        setDrawLineAction();
    }

    private void setButtonBallAction() {
        button_ball.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeBall();
            }
        });
    }

    private void setButtonLineAction() {
        button_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!lineActive) {
                    lineActive = true;
                    Toast.makeText(context, "plane을 클릭하면 선을 그립니다.", Toast.LENGTH_SHORT).show();
                } else {
                    lineActive = false;
                    Toast.makeText(context, "선 그리기 비활성화", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void makeBall() {
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

    private void setDrawLineAction() {
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                if (!lineActive) {
                    return;
                }

                if (!isClickedSecond) {
                    isClickedSecond = true;
                    previousPoint = new Vector3(hitResult.getHitPose().tx(),
                            hitResult.getHitPose().ty(),
                            hitResult.getHitPose().tz());

                    Log.d(TAG, "previousPosition   " + previousPoint.x + ", " + previousPoint.y + ", " + previousPoint.z);
                    Toast.makeText(context, "다음 지점을 선택해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                isClickedSecond = false;
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                Vector3 newPoint = anchorNode.getWorldPosition();
                Log.d(TAG, "newPosition  " + newPoint.x + ", " + newPoint.y + ", " + newPoint.z);

                Node node = new Node();

                Vector3 distanceVec3 = Vector3.subtract(previousPoint, newPoint);
                Vector3 directionVec3 = distanceVec3.normalized();
                Quaternion rotation = Quaternion.lookRotation(directionVec3, Vector3.up());


                Vector3 size = new Vector3(0.01f, 0.01f, distanceVec3.length());
                Vector3 center = Vector3.add(previousPoint, newPoint).scaled(0.5f);

                Color color_red = new Color(0xFFFF0000);
//                Renderable[] renderable = new Renderable[1];

                MaterialFactory.makeOpaqueWithColor(context, color_red)
                        .thenAccept(
                                material -> {
                                    ModelRenderable renderable;
                                    renderable = ShapeFactory.makeCube(size,
                                            Vector3.zero(), material);
                                    node.setParent(anchorNode);
                                    node.setRenderable(renderable);
                                    node.setWorldPosition(center);
                                    node.setWorldRotation(rotation);
                                });

//                node.setParent(anchorNode);
//                node.setRenderable(renderable);
//                node.setWorldPosition(center);
//                node.setWorldRotation(rotation);
            }
        });

    }


    private void setButtonPointCloudAction(){
        button_pointcloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(surfaceView.getVisibility() == View.GONE){
                    surfaceView.setVisibility(View.VISIBLE);
                }else{
                    surfaceView.setVisibility(View.GONE);
                }
            }
        });
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        try {
            pointCloudRenderer.createOnGlThread(/*context=*/ this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        try {
            ArSceneView arSceneView = arFragment.getArSceneView();
            Scene scene = arSceneView.getScene();
            Session session = arSceneView.getSession();
            Camera camera = scene.getCamera();

            Frame frame = session.update();

            PointCloud pointCloud = frame.acquirePointCloud();
            pointCloudRenderer.update(pointCloud);
            pointCloudRenderer.draw(camera.getViewMatrix().data, camera.getProjectionMatrix().data);

        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }
}

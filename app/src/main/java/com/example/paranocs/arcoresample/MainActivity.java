package com.example.paranocs.arcoresample;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private String TAG = getClass().getName();
    private Context context;
    private static final double MIN_OPENGL_VERSION = 3.0;

    private Button button_checkVersion;
    private Button button_start;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        button_checkVersion = findViewById(R.id.button_checkVersion);
        button_start = findViewById(R.id.button_start);

        setCheckVersionAction();
        setStartACtion();
    }


    private void setCheckVersionAction(){


        button_checkVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    Log.e(TAG, "Sceneform requires Android N or later");
                    Toast.makeText(context, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
                    return;
                }

                String openGlVersionString =
                        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                                .getDeviceConfigurationInfo()
                                .getGlEsVersion();
                if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                    Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
                    Toast.makeText(context, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                            .show();

                    return;
                }

                //모든 조건을 만족했으면 ARcore가 사용 가능합니다.
                button_start.setVisibility(View.VISIBLE);
                Toast.makeText(context, "시작 가능합니다", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void setStartACtion(){
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ArActivity 시작
                Intent intent = new Intent(MainActivity.this, ArActivity.class);
                startActivity(intent);
            }
        });

    }
}

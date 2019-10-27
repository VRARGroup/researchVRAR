package com.example.animal;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {
    ArFragment arFragment;
    private ModelRenderable modelRenderable;
    ViewRenderable name_animal;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check camera
        if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
        }

        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        setupModel();
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode= new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());


                createModel(anchorNode);
            }


        });

    }

    private void setupModel() {
        ModelRenderable.builder()
                .setSource(this, R.raw.spaceship)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast.makeText(this, "Unable to load Renderable.", Toast.LENGTH_LONG).show();
                            return null;
                        }
                        );
    }

    private void createModel(AnchorNode anchorNode) {
        TransformableNode animal= new TransformableNode(arFragment.getTransformationSystem());
        animal.setParent(anchorNode);
        animal.setRenderable(modelRenderable);
        animal.select();
    }
}

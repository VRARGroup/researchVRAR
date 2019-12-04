package com.example.animal;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {
    ArFragment arFragment;
    private ModelRenderable modelRenderable;
    ViewRenderable name_animal;
    final int REQUEST_CODE_SPEECH_INPUT = 199;
    ImageButton btnkey,btncam,btnspeak;
    EditText txt;
    HitResult hit;
    Anchor anchor;
    AnchorNode anchorNode=null;
    private Integer numberOfAnchors = 0;
    private List<AnchorNode> anchorNodeList = new ArrayList<>();
    private  static final int MAX_ANCHORS = 2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = findViewById(R.id.edit1);
        btnspeak=findViewById(R.id.ibtn3);
        btnspeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });


//        btncam.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(txt.getText().toString().isEmpty()) {
//                    Toast.makeText(MainActivity.this, "Chưa có dữ liệu", LENGTH_SHORT).show();
//                }
//                else {
//                    int resID = getResources().getIdentifier(replace(txt.getText().toString()), "raw", getPackageName());
//                    if(resID==0)
//                    {
//                        Toast.makeText(MainActivity.this, "Dữ liệu không tồn tại"+replace(txt.getText().toString()), LENGTH_SHORT).show();
//                    }
//                    else {
//                        setupModel(resID);
//                        arFragment.getArSceneView().clearAnimation();
//                        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
//                            @Override
//                            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
//                                hit=hitResult;
////                                Anchor anchor = hitResult.createAnchor();
////                                AnchorNode anchorNode= new AnchorNode(anchor);
////                                anchorNode.setParent(arFragment.getArSceneView().getScene());
////                                createModel(anchorNode);
//                            }
//
//
//                        });
//
//                    }
//                }
//
//            }
//        });
        //check camera
        if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
        }

        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getArSceneView().clearAnimation();
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

               if(hitResult!=null) {
                   hit = hitResult;
                   anchor = hit.createAnchor();
                   anchorNode = new AnchorNode(anchor);
                   if (numberOfAnchors < MAX_ANCHORS) {
                       Frame frame = arFragment.getArSceneView().getArFrame();
                       int currentAnchorIndex = numberOfAnchors;
                       Session session = arFragment.getArSceneView().getSession();
                       Anchor newMarkAnchor = session.createAnchor(
                               frame.getCamera().getPose()
                                       .compose(Pose.makeTranslation(0, 0, -3f))
                                       .extractTranslation());
                       AnchorNode addedAnchorNode = new AnchorNode(newMarkAnchor);
                       addedAnchorNode.setRenderable(modelRenderable);
                       addAnchorNode(addedAnchorNode);
                       anchorNode = addedAnchorNode;
                   }
               }
               else
                   Toast.makeText(MainActivity.this,"chạm vào màn hình để bắt mặt phẳng",Toast.LENGTH_LONG).show();
//                                Anchor anchor = hitResult.createAnchor();
//                                AnchorNode anchorNode= new AnchorNode(anchor);
//                                anchorNode.setParent(arFragment.getArSceneView().getScene());
//                                createModel(anchorNode);
            }

        });
    }

    private void setupModel(int id) {
        ModelRenderable.builder()
                .setSource(this, id)
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
        TransformableNode node= new TransformableNode(arFragment.getTransformationSystem());
//        animal.setParent(anchorNode);
//        animal.setRenderable(modelRenderable);
//        animal.select();
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.getScaleController().setMinScale(0.1f);
        node.getScaleController().setMaxScale(0.2f);
        node.getWorldModelMatrix();
        node.select();
    }
    void speak(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.EXTRA_LANGUAGE_MODEL);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                arFragment.getArSceneView().clearAnimation();
                anchorNode.setParent(null);
                anchorNode.setParent(arFragment.getArSceneView().getScene());
                createModel(anchorNode);
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }


        }
        catch (Exception g)
        {

        }

        int resID = getResources().getIdentifier(replace(txt.getText().toString()), "raw", getPackageName());
        if(resID>0) {
            setupModel(resID);
        }
        else
        {
            Toast.makeText(this,"chua co du lieu",LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d("SDV", results.get(0));

                    txt.setText(results.get(0));
                }
                break;
            }
        }
    }
    public String replace(String name){

        String result="";

        String temp = Normalizer.normalize(name, Normalizer.Form.NFD);

        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        result =  pattern.matcher(temp).replaceAll("");
        result = result.replace("'","''");
        result = result.replace(" ","");

        //result=result+""+name;
        //Replacing all non-alphanumeric characters with "_"
        //result = result.replaceAll("[^a-zA-Z0-9]+", "");
        return result;

    }
    private void addAnchorNode(AnchorNode nodeToAdd) {
        //Add an anchor node
        nodeToAdd.setParent(arFragment.getArSceneView().getScene());
        anchorNodeList.add(nodeToAdd);
        numberOfAnchors++;
    }
}

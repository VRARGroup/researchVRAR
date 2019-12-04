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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {
    ArFragment arFragment;
    private ModelRenderable modelRenderable;
    ViewRenderable name_animal;
    final int REQUEST_CODE_SPEECH_INPUT = 199;
    ImageButton btnkey,btncam,btnspeak;
    EditText txt;
    int resID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = findViewById(R.id.edit1);
        btnspeak = findViewById(R.id.ibtn3);
        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        btnspeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
        resID = getResources().getIdentifier(replace("bird"), "raw", getPackageName());
        setupModel(resID);
                arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
                    @Override
                    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                        Anchor anchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());
                        createModel(anchorNode);

                    }
                });

        //check camera
        if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
        }



       txt.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               if (txt.getText().toString().isEmpty()) {
                   Toast.makeText(MainActivity.this, "Chưa có dữ liệu", LENGTH_SHORT).show();
                   return;
               } else
               {
                   resID = getResources().getIdentifier(replace(txt.getText().toString()), "raw", getPackageName());
                   if (resID == 0) {
                       Toast.makeText(MainActivity.this, "Dữ liệu không tồn tại" + replace(txt.getText().toString()), LENGTH_SHORT).show();
                   } else {
                        setupModel(resID);
                   }
               }
           }

           @Override
           public void afterTextChanged(Editable s) {

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
        TransformableNode animal= new TransformableNode(arFragment.getTransformationSystem());
        animal.setParent(anchorNode);
        animal.getScaleController().setMinScale(0.1f);
        animal.getScaleController().setMaxScale(0.2f);
        animal.setLocalScale(new Vector3(0.4f,0.8f,0.4f));
        animal.setRenderable(modelRenderable);
        animal.select();
    }
    void speak(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.EXTRA_LANGUAGE_MODEL);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
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
}

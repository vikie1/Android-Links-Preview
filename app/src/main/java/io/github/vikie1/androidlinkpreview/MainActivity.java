package io.github.vikie1.androidlinkpreview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.material.color.DynamicColors;

import io.github.vikie1.linkpreview.Preview;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Preview portraitPreview = findViewById(R.id.portrait_link_preview);
        portraitPreview.setData("https://learnfromvictor.netlify.app/");
        portraitPreview.setListener(preview -> {});
    }
}
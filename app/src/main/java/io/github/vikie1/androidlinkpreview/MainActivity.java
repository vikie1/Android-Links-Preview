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

        Preview landscapePreview = findViewById(R.id.landscape_link_preview);
        landscapePreview.setData("https://learnfromvictor.netlify.app/blog/why-git-should-be-the-first-thing-a-developer-learns-and-not-code/");
        landscapePreview.setListener(preview -> {});

        Preview youtubePreview = findViewById(R.id.youtube_link_preview);
        youtubePreview.setData("https://youtu.be/WaE4townngA");
        youtubePreview.setListener(preview -> {});
    }
}
package com.github.vikie1.androidlinkpreview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.vikie1.linkpreview.Preview;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Preview portraitPreview = findViewById(R.id.portrait_link_preview);
        portraitPreview.setData("https://learnfromvictor.netlify.app/");

        Preview landscapePreview = findViewById(R.id.landscape_link_preview);
        landscapePreview.setData("https://learnfromvictor.netlify.app/blog/why-git-should-be-the-first-thing-a-developer-learns-and-not-code/");

        Preview youtubePreview = findViewById(R.id.youtube_link_preview);
        youtubePreview.setData("https://youtu.be/WaE4townngA");
    }
}
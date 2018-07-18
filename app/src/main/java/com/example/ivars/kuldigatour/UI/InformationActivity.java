package com.example.ivars.kuldigatour.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.example.ivars.kuldigatour.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InformationActivity extends AppCompatActivity {

    @BindView(R.id.home_page_layout)
    LinearLayout homePageLayout;

    @BindView(R.id.wikipedia_layout)
    LinearLayout wikipediaLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        ButterKnife.bind(this);

        homePageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPageIntent("http://kuldiga.lv/en/");
            }
        });

        wikipediaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPageIntent("https://en.wikipedia.org/wiki/Kuldīga");
            }
        });
    }

    private void openWebPageIntent(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}

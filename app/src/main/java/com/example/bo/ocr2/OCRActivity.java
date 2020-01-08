package com.example.bo.ocr2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class OCRActivity extends AppCompatActivity {

    private static final String TAG = "OCR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        TextView textView = (TextView)findViewById(R.id.text);

        Intent intent = getIntent();
        String data = intent.getStringExtra("ocr_data");
        Log.d(TAG, "onCreate: data is " + data);
        textView.setText(data);
    }
}

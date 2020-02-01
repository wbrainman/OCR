package com.example.bo.ocr2;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bo.ocr2.service.OcrService;

public class OCRActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView textView;
    private OcrService.OcrBinder ocrBinder;
    private OcrService ocrService;
    private static final String TAG = "OCR";

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ocrBinder = (OcrService.OcrBinder) service;
            ocrService = ocrBinder.getService();
            ocrService.setOcrListener(new OcrListener() {
                @Override
                public void onOcrResult(final String result) {
                    Log.d(TAG, getLocalClassName() + "onOcrResult: tid = " + Thread.currentThread().getId());
                    Log.d(TAG, getLocalClassName() + "onOcrResult: result = " + result);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(progressBar.getVisibility() == View.VISIBLE) {
                                progressBar.setVisibility(View.GONE);
                            }
//                            Log.d(TAG, getLocalClassName() + "run, onOcrResult: tid = " + Thread.currentThread().getId());
//                            Log.d(TAG, getLocalClassName() + "run, onOcrResult: result = " + result);
                            textView.setText(result);
                        }
                    });

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        Log.d(TAG, "onCreate: " + getLocalClassName() + " tid = " + Thread.currentThread().getId());

        textView = (TextView)findViewById(R.id.text);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);

        if(progressBar.getVisibility() == View.GONE) {
           progressBar.setVisibility(View.VISIBLE);
        }

        Intent bindIntent = new Intent(this, OcrService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: " + getLocalClassName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + getLocalClassName());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: " + getLocalClassName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: " + getLocalClassName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: " + getLocalClassName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }
        unbindService(connection);
        Log.d(TAG, "onDestroy: " + getLocalClassName());
    }
}

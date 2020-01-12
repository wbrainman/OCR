package com.example.bo.ocr2.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.bo.ocr2.MainActivity;
import com.example.bo.ocr2.OCRActivity;
import com.example.bo.ocr2.OcrListener;
import com.example.bo.ocr2.R;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

public class OcrService extends Service {

    private static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tesserart";
    private static final String DEFAULT_LANGUAGE = "chi_sim";
    private Bitmap mBitmap;
    OcrListener ocrListener;
    OcrBinder ocrBinder = new OcrBinder();
    private static final String TAG = "OCR";

    public OcrService() {
    }

    public class OcrBinder extends Binder {

        public OcrService getService() {
            return OcrService.this;
        }

    }
    @Override
    public IBinder onBind(Intent intent) {
        return ocrBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: tid = " + Thread.currentThread().getId());

        mBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.test);
        Log.d(TAG, "onCreate: size of bitmap is " + mBitmap.getByteCount());
        return super.onStartCommand(intent, flags, startId);
    }

    public void setOcrListener(OcrListener listener) {
        ocrListener = listener;
    }
    public void startOCR() {
        Log.d(TAG, "OCR start tid = " + Thread.currentThread().getId());
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: OCR start tid = " + Thread.currentThread().getId());
                TessBaseAPI tessBaseAPI = new TessBaseAPI();
                tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
                Log.d(TAG, "run: OCR init");
                tessBaseAPI.setImage(mBitmap);
                Log.d(TAG, "run: OCR  setImage");
                final String result = tessBaseAPI.getUTF8Text();
                //Log.d(TAG, "run: OCR result = " + result);

                ocrListener.onOcrResult(result);

                tessBaseAPI.end();
                Log.d(TAG, "run: OCR end");
            }
        }).start();
    }

}

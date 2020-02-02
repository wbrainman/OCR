package com.example.bo.ocr2.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
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

        return super.onStartCommand(intent, flags, startId);
    }

    public void setOcrListener(OcrListener listener) {
        ocrListener = listener;
    }

    public void startOCR(final Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: bitmap size = " + bitmap.getByteCount());
                TessBaseAPI tessBaseAPI = new TessBaseAPI();
                tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
                Log.d(TAG, "run: OCR init");
                tessBaseAPI.setImage(bitmap);
                Log.d(TAG, "run: OCR  setImage");
                final String result = tessBaseAPI.getUTF8Text();
                //Log.d(TAG, "run: OCR result = " + result);

                ocrListener.onOcrResult(result);

                tessBaseAPI.end();
                Log.d(TAG, "run: OCR end");
            }
        }).start();
    }

    /**
     * 转为二值图像
     *
     * @param bmp
     *            原图bitmap
     * @param w
     *            转换后的宽
     * @param h
     *            转换后的高
     * @param tmp
     *            二值化的阀值
     * @return
     */
    public  Bitmap convertToBMW(Bitmap bmp, int w, int h,int tmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        // 设定二值化的域值，默认值为100
        //tmp = 180;
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                // 分离三原色
                alpha = ((grey & 0xFF000000) >> 24);
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                if (red > tmp) {
                    red = 255;
                } else {
                    red = 0;
                }
                if (blue > tmp) {
                    blue = 255;
                } else {
                    blue = 0;
                }
                if (green > tmp) {
                    green = 255;
                } else {
                    green = 0;
                }
                pixels[width * i + j] = alpha << 24 | red << 16 | green << 8
                        | blue;
                if (pixels[width * i + j] == -1) {
                    pixels[width * i + j] = -1;
                } else {
                    pixels[width * i + j] = -16777216;
                }
            }
        }
        // 新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, w, h);
        return resizeBmp;
    }

    public Bitmap RemoveNoise(Bitmap bitmap) {
        for (int x = 0; x < bitmap.getWidth(); x ++) {
            for (int y = 0; y < bitmap.getHeight(); y ++) {
                int pixel = bitmap.getPixel(x,y);
                int R = Color.red(pixel);
                int G = Color.green(pixel);
                int B = Color.blue(pixel);
                if(R < 162 && G < 162 && B < 162) {
                   bitmap.setPixel(x,y,Color.BLACK);
                }
            }
        }

        for (int x = 0; x < bitmap.getWidth(); x ++) {
            for (int y = 0; y < bitmap.getHeight(); y ++) {
                int pixel = bitmap.getPixel(x,y);
                int R = Color.red(pixel);
                int G = Color.green(pixel);
                int B = Color.blue(pixel);
                if(R > 162 && G > 162 && B > 162) {
                    bitmap.setPixel(x,y,Color.WHITE);
                }
            }
        }
        return bitmap;
    }

}

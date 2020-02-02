package com.example.bo.ocr2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.bo.ocr2.service.OcrService;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jp.wasabeef.glide.transformations.GrayscaleTransformation;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OCR";
    private Button btnExport;
    private Button btnCamera;
    private Button btnLoad;
    private ImageView imageView;
    private Bitmap mBitmap;
    private Uri imageUri;
    private static final String[] permissions = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<String> permissionList = new ArrayList<>();
    private static final int CHOOSE_PHOTO = 1;
    private static final int TAKE_PHOTO = 2;
    private OcrService.OcrBinder ocrBinder;
    private OcrService ocrService;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ocrBinder = (OcrService.OcrBinder) service;
            ocrService = ocrBinder.getService();
            ocrService.setOcrListener(new OcrListener() {
                @Override
                public void onOcrResult(String result) {
                    Log.d(TAG, "onOcrResult: tid = " + Thread.currentThread().getId());
                    Log.d(TAG, "onOcrResult: result = " + result);

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
        setContentView(R.layout.activity_main);

        btnCamera = (Button) findViewById(R.id.camera);
        btnCamera.setOnClickListener(clickListener);
        btnExport = (Button) findViewById(R.id.export);
        btnExport.setOnClickListener(clickListener);
        btnLoad = (Button) findViewById(R.id.load);
        btnLoad.setOnClickListener(clickListener);
        imageView = (ImageView) findViewById(R.id.image);

        Glide.with(this).load(R.drawable.test).into(imageView);

        for(String permission:permissions) {
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if(permissionList.size() > 0) {
            requestPermissions(permissions, 0);
        }
        else {
            // TODO
        }

        Intent intent = new Intent(this, OcrService.class);
        startService(intent);
        Intent bindIntent = new Intent(this, OcrService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean denied = false;
        switch (requestCode) {
            case 0:
                for (int i = 0; i < grantResults.length; i ++) {
                    if(grantResults[i] == -1) {
                        denied = true;
                    }
                }
                if(denied) {
                    Toast.makeText(MainActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
                }
                else {
                    // TODO
                }
                break;
            default:
                break;
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.camera:
                    takePhoto();
                    break;
                case R.id.export:
                    Intent intent = new Intent(MainActivity.this, OCRActivity.class);
                    startActivity(intent);
                    ocrService.startOCR(mBitmap);
                    break;
                case R.id.load:
                    openAlbum();
                    break;
                default:
                    break;
            }
        }
    };

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult");
        switch (requestCode) {
            case TAKE_PHOTO:
                Log.d(TAG, "onActivityResult, req code is TAKE_PHOTO");
                if(RESULT_OK == resultCode) {
                    try {
                        Log.d(TAG, "onActivityResult RESULT_OK");
                        mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Glide.with(this).load(imageUri).into(imageView);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK) {
                    if(Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat(data);
                    }
                    else {
                        handleImageBeforeKitKat(data);
                    }
                }
            default:
                Log.d(TAG, "onActivityResult  NOT  RESULT_OK");
                break;
        }
    }

    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d(TAG, "handleImageOnKitKat: uri : " + uri );
        if(DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
            else if("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }
        else if("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        }
        else if("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }

        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;

        Cursor cursor = getContentResolver().query(uri,null, selection, null, null);
        if(null != cursor) {
            if(((Cursor) cursor).moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(final String imagePath) {
        if(imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Log.d(TAG, "displayImage: org bitmap w = " + bitmap.getWidth() + " h = " + bitmap.getHeight());
            Bitmap bmp = ocrService.convertToBMW(bitmap,400,400, 120);
            bmp = ocrService.RemoveNoise(bmp);
            mBitmap = bmp;
            Log.d(TAG, "displayImage: resize bitmap w = " + bmp.getWidth() + " h = " + bmp.getHeight());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bytes=baos.toByteArray();
            Glide.with(this).load(bytes).into(imageView);
        }
        else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePhoto() {
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        Log.d(TAG, "new file");
        try {
            if(outputImage.exists()) {
                Log.d(TAG, "image exist, delete");
                outputImage.delete();
            }
            Log.d(TAG, "outputImage.createNewFile");
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT >= 24) {
            Log.d(TAG, "FileProvider.getUriForFile");
            imageUri = FileProvider.getUriForFile(MainActivity.this,
                    "com.example.bo.ocr2.fileprovider", outputImage);
        }
        else {
            imageUri = Uri.fromFile(outputImage);
        }

        Log.d(TAG, "before startActivityForResult");
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}

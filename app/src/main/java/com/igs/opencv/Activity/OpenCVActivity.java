package com.igs.opencv.Activity;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.googlecode.tesseract.android.BuildConfig;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.igs.opencv.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenCVActivity extends AppCompatActivity{

    private static final int REQUEST_IMAGE1_CAPTURE = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private static final int PERMISSION_REQUEST_CODE = 100;
    private String mCurrentPhotoPath;
    private Uri photoURI1;
    private boolean flagPermissions;
    private ImageView firstImage;
    private TextView ocrText;
    private ProgressDialog mProgressDialog;
    private TesseractOCR mTessOCR;




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cvactivity);


        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed");
        } else {
            Log.d(TAG, "OpenCV initialization succeeded");
        }





        firstImage = findViewById(R.id.ocr_image);
        ocrText = findViewById(R.id.ocr_text);

        Button scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickScanButton();
            }
        });




        mTessOCR = new TesseractOCR(this, "eng");
        checkPermissions();




    }

    private void checkPermissions() {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        } else {
            flagPermissions = true;
        }
    }

    private boolean hasPermissions(Context context, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {

                openCamera();
            } else {
                Toast.makeText(this, "Permissions are required to use the camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("MMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            throw new IOException("Unable to access external files directory.");
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



    private void onClickScanButton() {

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        } else {

            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile(); // Create an image file
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error creating file: " + ex.getMessage());
            }
            if (photoFile != null) {
                photoURI1 = FileProvider.getUriForFile(this, "com.igs.opencv.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI1);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE1_CAPTURE);
            }
        } else {
            Toast.makeText(this, "No application available to handle camera intent", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE1_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath);
                if (bmp != null) {
                    firstImage.setImageBitmap(bmp);
                    doOCR(bmp);
                }
            } catch (Exception ex) {
                Log.e("TAG", "Error processing image: " + ex.getMessage());
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public class TesseractOCR {
        private final TessBaseAPI mTess;


        public TesseractOCR(Context context, String language) {
            mTess = new TessBaseAPI();
            String dstPathDir = context.getFilesDir() + "/tesseract/tessdata/";
            String srcFile = "eng.traineddata";
            File fileDir = new File(dstPathDir);
            File file = new File(dstPathDir + srcFile);

            try {
                if (!file.exists()) {
                    if (!fileDir.exists() && !fileDir.mkdirs()) {
                        Toast.makeText(context, "Directory for OCR data could not be created.", Toast.LENGTH_SHORT).show();
                    }
                    try (InputStream inFile = context.getAssets().open(srcFile);
                         OutputStream outFile = new FileOutputStream(file)) {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = inFile.read(buf)) != -1) {
                            outFile.write(buf, 0, len);
                        }
                    }
                }
                mTess.init(context.getFilesDir() + "/tesseract", language);
                mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
                mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);

               // mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);


            } catch (IOException ex) {
                Log.e(TAG, "Error initializing Tesseract: " + ex.getMessage());
                Toast.makeText(context, "Error initializing Tesseract OCR.", Toast.LENGTH_SHORT).show();
            }
        }

        public String getOCRResult(Bitmap bitmap) {
            mTess.setImage(bitmap);
            return mTess.getUTF8Text();
        }

        public void onDestroy() {
            if (mTess != null) mTess.end();
        }
    }


    private void doOCR(final Bitmap bitmap) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "Processing", "Doing OCR...", true);
        } else {
            mProgressDialog.show();
        }
        new Thread(new Runnable() {
            public void run() {
                final Bitmap preprocessedBitmap = preprocessImage(bitmap);
                final String srcText = mTessOCR.getOCRResult(preprocessedBitmap);
                final String cleanedText = cleanText(srcText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cleanedText != null && !cleanedText.isEmpty()) {
                            ocrText.setText(cleanedText);
                        }
                        mProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }





    private Bitmap preprocessImage(Bitmap bitmap) {
        // Convert Bitmap to Mat
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);

        // Convert the image to grayscale
        Mat grayMat = new Mat();
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY);

        // Apply Gaussian blur to reduce noise
        Mat blurredMat = new Mat();
        Imgproc.GaussianBlur(grayMat, blurredMat, new Size(3, 3), 0);

        // Apply adaptive thresholding to get a binary image
        Mat thresholdMat = new Mat();
        Imgproc.adaptiveThreshold(blurredMat, thresholdMat, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);

        // Optionally, use morphological operations to clean up the image
        Mat morphMat = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(thresholdMat, morphMat, Imgproc.MORPH_CLOSE, element);

        // Convert the processed Mat back to Bitmap
        Bitmap processedBitmap = Bitmap.createBitmap(morphMat.cols(), morphMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(morphMat, processedBitmap);

        // Release Mats to avoid memory leaks
        mat.release();
        grayMat.release();
        blurredMat.release();
        thresholdMat.release();
        morphMat.release();
        element.release();

        return processedBitmap;
    }

    private String cleanText(String text) {

        text = text.replaceAll("[^a-zA-Z0-9\\s]", ""); // Remove non-alphanumeric characters except spaces

        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }




}
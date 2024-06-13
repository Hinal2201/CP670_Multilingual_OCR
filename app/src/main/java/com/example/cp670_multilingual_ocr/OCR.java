package com.example.cp670_multilingual_ocr;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;


public class OCR extends AppCompatActivity {
    private SurfaceView cameraPreview;
    private Button captureButton;
    private Camera mCamera;
    private CameraPreview mPreview;

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private void initializeCamera() {
        // Safe way to get an instance of the Camera object.
        mCamera = getCameraInstance();

        if (mCamera == null) {
            // Camera is not available, show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Camera is not available.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button, return to MainActivity
                            Intent intent = new Intent(OCR.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Finish the current activity
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return; // Exit the method early
        }
    
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.e("Camera", "Camera is not available: " + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    /** A basic Camera preview class */
    class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d("CameraPreview", "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.
            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception ignored) {
                // ignore: tried to stop a non-existent preview
            }

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                Log.d("CameraPreview", "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        cameraPreview = findViewById(R.id.camera_preview);
        captureButton = findViewById(R.id.capture_button);

        // Initialize your camera and start preview
        // Remember to check for camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            initializeCamera();
        }

        captureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO: Capture photo and call MLKit to process the image
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera();
            }
            else {
                //TODO: Eror handling when permission was denied.
            }
        }
    }

}
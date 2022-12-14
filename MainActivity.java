package com.example.testjni;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.view.TextureView;
import android.view.Surface;
import java.util.Arrays;
import android.os.HandlerThread;
import android.os.Handler;
import java.util.*;
import android.util.Size;
import android.graphics.*;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;
import android.media.Image;
import java.util.Collections;
import android.annotation.SuppressLint;
import java.nio.ByteBuffer;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private TextureView mTextureView;
    private Button mButton;
    private ImageView mImageView;
    private ImageReader mImageReader;
    private CameraManager mCameraManager;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private String mCameraid;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;
    private final int RESULT_CODE_CAMERA=1;
    private ImageReader imageReader;
    private Size mPreviewsize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                 //??????????????????
        mTextureView = findViewById(R.id.textureView);          //??????????????????
        mButton = findViewById(R.id.button);                    //??????????????????
        mImageView = findViewById(R.id.imageView);
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);  //??????manager

        //??????????????????
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        startBackground();
        if(mTextureView.isAvailable()){
            openCamera(mTextureView.getWidth(),mTextureView.getHeight());
        }else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
    }
    /************************************????????????************************************************************/
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {       //textureview????????????

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            //configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            // NOOP
        }
    };

    private final CameraDevice.StateCallback devicecallback = new CameraDevice.StateCallback() {                //??????camera??????
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    /**************************************????????????********************************************************/
    @SuppressLint("MissingPermission")
    private void openCamera(int width,int height){
        setUpCamera(width,height);
        try{
            mCameraManager.openCamera(mCameraid, devicecallback, mBackgroundHandler);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createPreviewSession(){
        try {
            //TODO?????????capturesession???capturerequest,????????????
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            Surface surface = new Surface(texture);
            List<Surface> targets = Arrays.asList(surface,mImageReader.getSurface());   //????????????????????????????????????

            //??????capture??????
            CaptureRequest.Builder previewbuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewbuilder.addTarget(surface);
            //??????capture??????
            mCameraDevice.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {      //targets????????????session?????????????????????????????????addtarget????????????????????????????????????
                @Override                                                                               //???????????????????????????capture????????????????????????????????????
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mCaptureSession = cameraCaptureSession;
                    try {
                        mPreviewRequest = previewbuilder.build();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest,null,mBackgroundHandler);
                    }catch (CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mCaptureSession = null;
                }
            }, null);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startBackground(){
        mBackgroundThread = new HandlerThread("camerabackground");      //??????camerabackground?????????,??????????????????camera??????
        mBackgroundThread.start();                                            //????????????
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());      //??????
    }

    private void takePicture(){
        try {
            CaptureRequest.Builder picturebuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            picturebuilder.addTarget(mImageReader.getSurface());
            mCaptureSession.stopRepeating();
            CaptureRequest captureRequest = picturebuilder.build();
            mCaptureSession.capture(captureRequest, new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    try {
                        mCaptureSession.setRepeatingRequest(mPreviewRequest,null,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            },mBackgroundHandler);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setUpCamera(int width,int height){
        try {
            for(String cameraid : mCameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraid);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int cameraLensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(map == null){
                    continue;
                }

                //Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),new CompareSizesByArea());
                //mPreviewsize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),width,height,largest);

                if(cameraLensFacing == CameraMetadata.LENS_FACING_BACK) {           //????????????
                    mCameraid = cameraid;
                    Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),new CompareSizesByArea());
                    mImageReader = ImageReader.newInstance(largest.getWidth(),largest.getHeight(),ImageFormat.JPEG,2);
                    mImageReader.setOnImageAvailableListener(ImageAvailableListener,null);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private ImageReader.OnImageAvailableListener ImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                mImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static Size chooseOptimalSize(Size[] choices
            , int width, int height, Size aspectRatio)
    {
        // ????????????????????????????????????Surface????????????
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices)
        {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height)
            {
                bigEnough.add(option);
            }
        }
        // ????????????????????????????????????????????????????????????
        if (bigEnough.size() > 0)
        {
            return Collections.min(bigEnough, new CompareSizesByArea());
        }
        else
        {
            //???????????????????????????
            return choices[0];
        }
    }

    static class CompareSizesByArea implements Comparator<Size>
    {
        @Override
        public int compare(Size lhs, Size rhs)
        {
            // ?????????long????????????????????????
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

}

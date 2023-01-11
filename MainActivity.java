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
        setContentView(R.layout.activity_main);                 //加载界面布局
        mTextureView = findViewById(R.id.textureView);          //读取显示控件
        mButton = findViewById(R.id.button);                    //读取按钮控件
        mImageView = findViewById(R.id.imageView);
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);  //获取manager

        //拍照按钮响应
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
    /************************************回调区间************************************************************/
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {       //textureview监听回调

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

    private final CameraDevice.StateCallback devicecallback = new CameraDevice.StateCallback() {                //打开camera回调
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

    /**************************************自定函数********************************************************/
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
            //TODO：创建capturesession，capturerequest,发送请求
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            Surface surface = new Surface(texture);
            List<Surface> targets = Arrays.asList(surface,mImageReader.getSurface());   //预览缓冲与图片存储缓冲区

            //创建capture请求
            CaptureRequest.Builder previewbuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewbuilder.addTarget(surface);
            //创建capture会话
            mCameraDevice.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {      //targets是设置该session的所有图像输出缓冲区，addtarget是设置某一帧的输出缓冲区
                @Override                                                                               //在一个会话中不同的capture可以设置不同的输出缓冲区
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
        mBackgroundThread = new HandlerThread("camerabackground");      //名为camerabackground的线程,用于做耗时的camera操作
        mBackgroundThread.start();                                            //线程启动
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());      //绑定
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

                if(cameraLensFacing == CameraMetadata.LENS_FACING_BACK) {           //打开后置
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
        // 收集摄像头支持的大过预览Surface的分辨率
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
        // 如果找到多个预览尺寸，获取其中面积最小的
        if (bigEnough.size() > 0)
        {
            return Collections.min(bigEnough, new CompareSizesByArea());
        }
        else
        {
            //没有合适的预览尺寸
            return choices[0];
        }
    }

    static class CompareSizesByArea implements Comparator<Size>
    {
        @Override
        public int compare(Size lhs, Size rhs)
        {
            // 强转为long保证不会发生溢出
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

}

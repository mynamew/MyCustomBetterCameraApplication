package com.example.huanbei_dev4.mycustombettercameraapplication.camera;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.view.SurfaceHolder;

import com.example.huanbei_dev4.mycustombettercameraapplication.camera.utils.AngleUtil;
import com.example.huanbei_dev4.mycustombettercameraapplication.camera.utils.CameraParamUtil;

import java.io.IOException;
import java.util.List;

/**
 * 相机的单例
 *
 * @autor timi
 * create at 2017/6/2 10:19
 */

public class CameraSingleton {

    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;//是否在预览
    private static CameraSingleton instance;//单例

    boolean isPreviewing() {
        return isPreviewing;
    }

    private int SELECTED_CAMERA = -1;
    private int CAMERA_POST_POSITION = -1;
    private int CAMERA_FRONT_POSITION = -1;

    private SurfaceHolder mHolder = null;
    private float screenProp = -1.0f;
    //预览的宽高
    private int preview_width;
    private int preview_height;
    //角度 旋转
    private int angle = 0;
    private int rotation = 0;
   //构造方法
    private CameraSingleton() {
        findAvailableCameras();
        SELECTED_CAMERA = CAMERA_POST_POSITION;
    }

    //获取实例
    public static synchronized CameraSingleton getInstance() {
        if (instance == null) {
            instance = new CameraSingleton();
        }
        return instance;
    }

    /**
     * 获取角度
     */
    private SensorManager sm = null;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }
            float[] values = event.values;
            angle = AngleUtil.getSensorAngle(values[0], values[1]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    /**
     * 找到可用的谁想投
     */
    private void findAvailableCameras() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraNum = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNum; i++) {
            Camera.getCameraInfo(i, info);
            switch (info.facing) {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    CAMERA_FRONT_POSITION = info.facing;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    CAMERA_POST_POSITION = info.facing;
                    break;
            }
        }
    }

    /**
     * 打开摄像头
     *
     * @param callback
     */
    void doOpenCamera(CamOpenOverCallback callback) {
        if (mCamera == null) {
            openCamera(SELECTED_CAMERA);
        }
        callback.cameraHasOpened();
    }

    /**
     * 打开摄像头（前置  后置）
     * @param id
     */
    private void openCamera(int id) {
        mCamera = Camera.open(id);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mCamera.enableShutterSound(false);
        }
    }

    /**
     * 前置 后置摄像头切换
     * @param callback
     */
    public synchronized void switchCamera(CamOpenOverCallback callback) {
        if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
            SELECTED_CAMERA = CAMERA_FRONT_POSITION;
        } else {
            SELECTED_CAMERA = CAMERA_POST_POSITION;
        }
        doStopCamera();
        mCamera = Camera.open(SELECTED_CAMERA);
        doStartPreview(mHolder, screenProp);
        callback.cameraSwitchSuccess();
    }
    /**
     * doStartPreview
     */
    void doStartPreview(SurfaceHolder holder, float screenProp) {
        if (this.screenProp < 0) {
            this.screenProp = screenProp;
        }
        if (holder == null) {
            return;
        }
        this.mHolder = holder;
        if (mCamera != null) {
            try {
                mParams = mCamera.getParameters();
                Camera.Size previewSize = CameraParamUtil.getInstance().getPreviewSize(mParams
                        .getSupportedPreviewSizes(), 1000, screenProp);
                Camera.Size pictureSize = CameraParamUtil.getInstance().getPictureSize(mParams
                        .getSupportedPictureSizes(), 1200, screenProp);

                mParams.setPreviewSize(previewSize.width, previewSize.height);

                preview_width = previewSize.width;
                preview_height = previewSize.height;

                mParams.setPictureSize(pictureSize.width, pictureSize.height);

                if (CameraParamUtil.getInstance().isSupportedFocusMode(mParams.getSupportedFocusModes(), Camera
                        .Parameters.FOCUS_MODE_AUTO)) {
//                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    //实现Camera自动对焦
                    List<String> focusModes = mParams.getSupportedFocusModes();
                    if (focusModes != null) {
                        for (String mode : focusModes) {
                            mode.contains("continuous-video");
                            mParams.setFocusMode("continuous-video");
                        }
                    }
                }
                if (CameraParamUtil.getInstance().isSupportedPictureFormats(mParams.getSupportedPictureFormats(),
                        ImageFormat.JPEG)) {
                    mParams.setPictureFormat(ImageFormat.JPEG);
                    mParams.setJpegQuality(100);
                }
                mCamera.setParameters(mParams);
                mParams = mCamera.getParameters();
                //SurfaceView
                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
                isPreviewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                mCamera.stopPreview();
            }
        }
    }

    /**
     * 停止预览，释放Camera
     */
    public void doStopCamera() {
        if (null != mCamera) {
            try {
                mCamera.setPreviewDisplay(null);
                mCamera.stopPreview();
                isPreviewing = false;
                mCamera.release();
                mCamera = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void doDestroyCamera() {
        if (null != mCamera) {
            try {
                mCamera.setPreviewDisplay(null);
                mCamera.stopPreview();
                mHolder = null;
                isPreviewing = false;
                mCamera.release();
                mCamera = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 拍照
     */

    public void takePicture(final TakePictureCallback callback) {
        final int nowAngle = (angle + 90) % 360;
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                    matrix.setRotate(nowAngle);
                } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
                    matrix.setRotate(270);
                    matrix.postScale(-1, 1);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (callback != null) {
                    callback.captureResult(bitmap);
                }
            }
        });
    }

    /**
     * 自动对焦
     */
    public void autoCameraFocus(){
       if(null!=mCamera){
           //实现自动对焦
           mCamera.autoFocus(new Camera.AutoFocusCallback() {
               @Override
               public void onAutoFocus(boolean success, Camera camera) {
                   if (success) {
                       camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                   }
               }
           });
       }
    }
    /**
     * 注册传感器管理器
     * @param context
     */
    public void registerSensorManager(Context context) {
        if (sm == null) {
            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sm.registerListener(sensorEventListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager
                .SENSOR_DELAY_NORMAL);
    }

    /**
     * 注册
     * @param context
     */
    public void unregisterSensorManager(Context context) {
        if (sm == null) {
            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sm.unregisterListener(sensorEventListener);
    }

    /**
     * 打开摄像头的回调接口
     */
    public interface CamOpenOverCallback {
        void cameraHasOpened();

        void cameraSwitchSuccess();
    }

    /**
     * 拍照回调的接口
     */
    public interface TakePictureCallback {
        void captureResult(Bitmap bitmap);
    }
}

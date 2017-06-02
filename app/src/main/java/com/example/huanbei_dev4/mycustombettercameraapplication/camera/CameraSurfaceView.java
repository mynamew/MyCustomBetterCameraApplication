package com.example.huanbei_dev4.mycustombettercameraapplication.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


import com.example.huanbei_dev4.mycustombettercameraapplication.camera.utils.ImageTools;
import com.example.huanbei_dev4.mycustombettercameraapplication.camera.utils.PathUtils;
import com.example.huanbei_dev4.mycustombettercameraapplication.camera.utils.ToastUtils;

import java.io.IOException;
import java.util.List;

/**
 * 自定义的相机预览 surfaceview
 *
 * @autor timi
 * create at 2017/5/15 16:19
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static String TAG = "ren.solid.camerademo.view.CameraSurfaceView";
    private SurfaceHolder mSurfaceHolder;
    private Camera camera;
    private Camera.Parameters parameters = null;
    private Context mContext;
    private int mCameraCount;
    private int mCurrentCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private String mPictureSaveDir;
    private OnSavePictureListener mOnSavePictureListener;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }


    private void init() {
        mCameraCount = Camera.getNumberOfCameras();//得到摄像头数量
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.setFixedSize(640, 480);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setKeepScreenOn(true);// 屏幕常亮
        if (mCurrentCameraFacing == 0) {
            //实现Camera自动对焦
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (camera != null) {
                        camera.autoFocus(null);
                    }
                }
            });
        }
        getCamera();
    }

    /***
     * 得到系统相机
     */
    private void getCamera() {
        if (camera == null)
            camera = Camera.open(mCurrentCameraFacing); // 打开后置摄像头
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startPreView(mSurfaceHolder);
    }

    // 拍照状态变化时调用该方法
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (camera != null) {
            if (mCurrentCameraFacing == 0) {
                //实现自动对焦
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                        }
                    }
                });
            }

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();
    }

    /**
     * 开启预览
     *
     * @param holder
     */
    private void startPreView(SurfaceHolder holder) {
        try {
            if (camera == null) {
                camera = Camera.open(mCurrentCameraFacing);
            }
            if (camera != null) {
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(holder);
                Camera.Parameters parameters = camera.getParameters();
                if (mCurrentCameraFacing == 0) {
                    //实现Camera自动对焦
                    List<String> focusModes = parameters.getSupportedFocusModes();
                    if (focusModes != null) {
                        for (String mode : focusModes) {
                            mode.contains("continuous-video");
                            parameters.setFocusMode("continuous-video");
                        }
                    }
                }
//                List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获取所有支持的camera尺寸
                DisplayMetrics dm = mContext.getApplicationContext().getResources().getDisplayMetrics();
                Camera.Size optionSize = getPreviewOptimalSize(camera, dm.widthPixels, dm.heightPixels);//获取一个最为适配的camera.size
                parameters.setPreviewSize(optionSize.width, optionSize.height);//把camera.size赋值到parameters


//                List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
//                Camera.Size m_camSize = getPreviewOptimalSize(mCamera, screenWidth, (int) (screenWidth * (0.7)));
                ViewGroup.LayoutParams lp = getLayoutParams();
                DisplayMetrics displaymetrics = new DisplayMetrics();
                ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                lp.width = displaymetrics.widthPixels;
                lp.height = (int) (lp.width * (optionSize.width / (optionSize.height * 1f)));
                setLayoutParams(lp);
                camera.setDisplayOrientation(getPreviewDegree());
                camera.setParameters(parameters);
                camera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /***
     * 切换相机摄像头
     */
    public void changCameraFacing() {
        if (mCameraCount > 1) {
//            //切换前后摄像头
//            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//            for (int i = 0; i < mCameraCount; i++) {
//                Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
//                if (mCurrentCameraFacing == 0) {
//                    //现在是后置，变更为前置
//                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
//                        camera.stopPreview();//停掉原来摄像头的预览
//                        camera.release();//释放资源
//                        camera = null;//取消原来摄像头
////                        camera = Camera.open(i);//打开当前选中的摄像头
//                        startPreView(mSurfaceHolder);
//                        mCurrentCameraFacing = 1;
//                        break;
//                    }
//                } else {
//                    //现在是前置， 变更为后置
//                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
//                        camera.stopPreview();//停掉原来摄像头的预览
//                        camera.release();//释放资源
//                        camera = null;//取消原来摄像头
////                        camera = Camera.open(i);//打开当前选中的摄像头
//                        startPreView(mSurfaceHolder);
//                        mCurrentCameraFacing = 0;
//                        break;
//                    }
//                }
//
//            }
            mCurrentCameraFacing = (mCurrentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) ?
                    Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
            releaseCamera();
            startPreView(mSurfaceHolder);
        } else {
            //手机不支持前置摄像头
            ToastUtils.showLong(mContext, "很抱歉，您的手机不支持前置摄像头~");
        }
    }

    /**
     * 用于根据手机方向获得相机预览画面旋转的角度
     */
    private int getPreviewDegree() {
        // 获得手机的方向
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    /**
     * 将拍下来的照片存放在SD卡中
     *
     * @param data
     * @throws IOException
     */
    private void saveToSDCard(final byte[] data) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //由于在预览的时候，我们调整了预览的方向，所以在保存的时候我们要旋转回来，不然保存的图片方向是不正确的
                Matrix matrix = new Matrix();
                if (mCurrentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    matrix.setRotate(90);
                } else {
                    matrix.setRotate(-90);
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

                final String path = PathUtils.getSaveImgPath(mContext) + "/publish" + System.currentTimeMillis() + ".jpg";
                final Bitmap finalBitmap = bitmap;
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mOnSavePictureListener != null) {
                            mOnSavePictureListener.onSuccess(finalBitmap, path);
                        }
                    }
                });
//                ((SuperActivity) mContext).dissmissProgress();

                ImageTools.savePhotoToSDCard(finalBitmap, (Activity) mContext,path, false, new ImageTools.ImageSave() {
                    @Override
                    public void success() {
                        //刷新Ui
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mOnSavePictureListener != null) {
                                    mOnSavePictureListener.onSuccessSavaImg(path);
                                }
                                releaseCamera();
                                startPreView(mSurfaceHolder);
                            }
                        });
                    }

                    @Override
                    public void failuer() {

                    }
                });
            }
        }).start();
    }

    /**
     * 获取合适的预览大小
     *
     * @param cam
     * @param w
     * @param h
     * @return
     */
    public Camera.Size getPreviewOptimalSize(Camera cam, int w, int h) {
        List<Camera.Size> sizes = cam.getParameters().getSupportedPreviewSizes();
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;

        // Start with max value and refine as we iterate over available preview sizes. This is the
        // minimum difference between view and camera height.
        double minDiff = Double.MAX_VALUE;

        // Target view height
        int targetHeight = h;

        // Try to find a preview size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find preview size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        Camera.Parameters pm = cam.getParameters();
        pm.setPreviewSize(optimalSize.width, optimalSize.height);
        cam.setParameters(pm);

        return optimalSize;
    }

    private final class MyCameraPictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                saveToSDCard(data); // 保存图片到sd卡中
//                camera.startPreview(); // 拍完照后，重新开始预览
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拍照
     */
    public void takePicture() {
        if (camera != null) {
//            ((SuperActivity) mContext).showProgress();
            camera.takePicture(null, null, new MyCameraPictureCallback());//每次调用takePicture获取图像后，摄像头会停止预览
        } else {
            //TODO: 提示用户相机不存在
        }
    }

    /***
     * 释放相机资源
     */
    public void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * 设置图片的保存路径
     *
     * @param pictureSavePath
     */
    public void setPictureSavePath(String pictureSavePath) {
        mPictureSaveDir = pictureSavePath;
    }

    /***
     * 得到图片保存的目录
     *
     * @return
     */
    public String getPictureSaveDir() {
        String path;
        if (mPictureSaveDir == null) {
            path = PathUtils.getSaveImgPath(mContext);
        } else {
            path = mPictureSaveDir;
        }
        mPictureSaveDir = path;
        return path;
    }

    /**
     * 设置相机的闪光灯
     *
     * @param isOpen
     */
    public void setCameraFlashLight(boolean isOpen) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(isOpen ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF);//开启
        camera.setParameters(parameters);
    }

    public void onResume() {
        getCamera();
    }

    public void onPause() {
        releaseCamera();
    }

    public void setOnSavePictureListener(OnSavePictureListener onSavePictureListener) {
        mOnSavePictureListener = onSavePictureListener;
    }

    public interface OnSavePictureListener {
        void onSuccess(Bitmap bitmap, String filePath);

        void onSuccessSavaImg(String filePath);
    }
}

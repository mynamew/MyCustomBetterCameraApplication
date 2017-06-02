package com.example.huanbei_dev4.mycustombettercameraapplication.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.widget.RelativeLayout;
import android.widget.VideoView;

/** 
 * 自定义的Cameraview
 * @autor timi
 * create at 2017/6/2 10:44 
 */
public class CameraView extends RelativeLayout implements CameraSingleton.CamOpenOverCallback, SurfaceHolder.Callback{
    private Context mContext;
//    //显示拍照后的图片
//    private ImageView mPhoto;
    //拍照后保存图片的Bitmap
    private Bitmap captureBitmap;
    //屏幕长宽比
    private float screenProp;
    //用于预览
    private VideoView mVideoView;
    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }
    private void initView() {
        setWillNotDraw(false);
        this.setBackgroundColor(0xff000000);
        //VideoView
        mVideoView = new VideoView(mContext);
        LayoutParams videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        videoViewParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mVideoView.setLayoutParams(videoViewParam);
        this.addView(mVideoView);
        mVideoView.getHolder().addCallback(this);
    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        new Thread() {
            @Override
            public void run() {
                CameraSingleton.getInstance().doOpenCamera(CameraView.this);
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
//        CameraSingleton.getInstance().autoCameraFocus();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        CameraSingleton.getInstance().doDestroyCamera();
    }

    @Override
    public void cameraHasOpened() {
        CameraSingleton.getInstance().doStartPreview(mVideoView.getHolder(), screenProp);
    }
    private boolean switching = false;
    @Override
    public void cameraSwitchSuccess() {
        switching = false;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = MeasureSpec.getSize(widthMeasureSpec);
        float heightSize = MeasureSpec.getSize(heightMeasureSpec);
        screenProp = heightSize / widthSize;
    }
    /**
     * start preview
     */
    public void onResume() {
        CameraSingleton.getInstance().registerSensorManager(mContext);
    }

    /**
     * stop preview
     */
    public void onPause() {
        CameraSingleton.getInstance().unregisterSensorManager(mContext);
        CameraSingleton.getInstance().doStopCamera();
    }
}

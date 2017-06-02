package com.example.huanbei_dev4.mycustombettercameraapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huanbei_dev4.mycustombettercameraapplication.baseadapter.BaseNoFooterAndHeaderRecyclerAdapter;
import com.example.huanbei_dev4.mycustombettercameraapplication.camera.CameraSingleton;
import com.example.huanbei_dev4.mycustombettercameraapplication.camera.CameraView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private CameraView cameraView;
    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    private boolean granted = false;
    private ImageView iv_picture;
    private RecyclerView rlvImg;
    private ImageView ivImg;
    private TextView tvTitle, tvAuto;
    private BaseNoFooterAndHeaderRecyclerAdapter<ImageItem> adapter;
    private int MaxTakePictureNum = 8;//拍照的最大数量
    private boolean isShowConfirmLayout = false;//是否是在 确认拍照的布局
    private boolean isAutoFlashLight = false;//是否开启闪光灯
    private ArrayList<ImageItem> imgs;//已经保存的拍照的图片的数量
    private ArrayList<String> takeImgs;//拍照的图片的数量
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView= (CameraView) findViewById(R.id.camera);
        iv_picture= (ImageView) findViewById(R.id.iv_picture);
        getPermissions();

        imgs = new ArrayList<>();
        takeImgs = new ArrayList<>();
        tvTitle = (TextView) findViewById(R.id.tv_confirm_img_title);
        tvAuto = (TextView) findViewById(R.id.tv_auto);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            cameraView.onResume();
        } else {
            if (granted) {
                cameraView.onResume();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
        Log.i("JCameraView", "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraSingleton.getInstance().doDestroyCamera();
        Log.i("JCameraView", "onDestroy");
    }
    /**
     * 获取权限
     */
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                    .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager
                            .PERMISSION_GRANTED) {
                //具有权限
                granted = true;
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA}, GET_PERMISSION_REQUEST);
                granted = false;
            }
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                //读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }
                if (size == 0) {
                    granted = true;
                    cameraView.onResume();
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    /**
     * 拍照的点击事件
     *
     * @param view
     */
    public void takePicClick(View view) {
        switch (view.getId()) {
            case R.id.takepicture:       //拍照
                /**
                 * 如果拍照的数量达到上限 则不能继续拍照
                 */
                if (takeImgs.size() >= MaxTakePictureNum) {
                   Toast.makeText(MainActivity.this,"只能拍8张图片",Toast.LENGTH_SHORT).show();
                    return;
                }
                /**
                 * 执行拍照 并且将拍照按钮设置不可点击
                 */
                findViewById(R.id.takepicture).setClickable(false);
                CameraSingleton.getInstance().takePicture(new CameraSingleton.TakePictureCallback() {
                    @Override
                    public void captureResult(Bitmap bitmap) {
                        iv_picture.setImageBitmap(bitmap);
                        //设置显示  是否确认的布局
                        setLayoutStatus(true);
                    }
                });
                break;
            case R.id.tv_cancel_take_pic://取消
                onBackPressed();
                break;
            case R.id.tv_finish_take_pic://完成
                if (takeImgs.size() != imgs.size()) {
                    return;
                }
                /**
                 * 返回数据
                 */
                this.onBackPressed();
                break;
            case R.id.iv_cancel:         //取消拍照
                setLayoutStatus(false);
                break;
            case R.id.iv_back:         //是否确认状态下返回
                delTakePictures();
                break;
            case R.id.iv_confirm:        //确认拍照
                setLayoutStatus(false);
                break;
            case R.id.tv_auto:           //自动
            case R.id.iv_auto:
                break;
            case R.id.iv_rotation_camera://镜头切换
                //前置后置摄像头切换
                CameraSingleton.getInstance().switchCamera(new CameraSingleton.CamOpenOverCallback() {
                    @Override
                    public void cameraHasOpened() {

                    }

                    @Override
                    public void cameraSwitchSuccess() {

                    }
                });
                break;
        }
    }
    /**
     * 设置 布局的状态（拍照和是否确认）
     *
     * @param isShowConfirmPhtoGraph
     */

    public void setLayoutStatus(boolean isShowConfirmPhtoGraph) {
        //设置标题的文本
        tvTitle.setText(takeImgs.size() + "/" + MaxTakePictureNum);
        //拍照的头部
        findViewById(R.id.rl_take_staus).setVisibility(isShowConfirmPhtoGraph ? View.VISIBLE : View.GONE);
        //是否确认的头部
        findViewById(R.id.rl_take_photo).setVisibility(isShowConfirmPhtoGraph ? View.GONE : View.VISIBLE);
        //是否确认的底部
        findViewById(R.id.ll_choose_picture).setVisibility(isShowConfirmPhtoGraph ? View.VISIBLE : View.GONE);
        //拍照的底部
        findViewById(R.id.rl_take_picture_bottom).setVisibility(isShowConfirmPhtoGraph ? View.GONE : View.VISIBLE);
        //surfaceview
        findViewById(R.id.camera).setVisibility(isShowConfirmPhtoGraph ? View.GONE : View.VISIBLE);
        //判断是否有图片 再去显示recycleview
        //图片的recycleview
        if (imgs.size() > 0) {
            findViewById(R.id.rlv_img).setVisibility(isShowConfirmPhtoGraph ? View.GONE : View.VISIBLE);
        }
        //图片的预览
        findViewById(R.id.iv_picture).setVisibility(isShowConfirmPhtoGraph ? View.VISIBLE : View.GONE);
        findViewById(R.id.takepicture).setClickable(!isShowConfirmPhtoGraph);
    }
    @Override
    public void onBackPressed() {
        if (isShowConfirmLayout) {//如果是确认状态下 返回显示拍照界面
            //如果取消的图片已经保存过 则需要在链表中删除
            takeImgs.remove(takeImgs.size() - 1);
            setLayoutStatus(false);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                if (!deleteDir(new File(dir, children[i]))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    /**
     * 删除 拍照的图片
     */
    private void delTakePictures() {
        //如果取消的图片已经保存过 则需要在链表中删除
        if(takeImgs.size()==imgs.size()){
            deleteDir(new File(imgs.get(imgs.size()-1).sourcePath));
            imgs.remove(takeImgs.size() - 1);
            if (imgs.size() > 0) {
                rlvImg.setVisibility(View.VISIBLE);
            } else {
                rlvImg.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        }
        takeImgs.remove(takeImgs.size() - 1);
        setLayoutStatus(false);
    }
    /**
     * 判断是否删除图片的方法
     * @param filePath
     */
    public void adjustDelImg(String filePath){
        if (takeImgs.isEmpty()) {//如果链表为空 则直接删除刚刚保存的图片
            deleteDir(new File(filePath));
        } else {
            for (int i = 0; i < takeImgs.size(); i++) {
                if (filePath.equals(takeImgs.get(i))) {
                    //图片转换
                    imgs.add(new ImageItem(filePath));
                    break;
                }
                if (i == takeImgs.size() - 1) {
                    deleteDir(new File(filePath));
                }
            }
            if (imgs.size() > 0) {
                rlvImg.setVisibility(View.VISIBLE);
            } else {
                rlvImg.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        }
    }
}

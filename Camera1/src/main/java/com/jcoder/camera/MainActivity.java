package com.jcoder.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera.CameraInfo;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    Context mContext;
    SurfaceView surfaceView;
    SurfaceHolder mSurfaceHolder;
    Camera camera;
    private static final int FRONT = 1;//前置摄像头标记
    private static final int BACK = 2;//后置摄像头标记
    private int currentCameraType = -1;//当前打开的摄像头标记

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        surfaceView = findViewById(R.id.surface_camera);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        camera = openCamera(BACK);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        closeCamera();
    }

    public Camera openCamera(int type) {
        Log.d("pepe", "===> openCamera");
        //摄像头成像角度
        int cameraOrientation = 90;
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        CameraInfo info = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                frontIndex = cameraIndex;
                Log.d("pepe", "===> frontIndex = " + frontIndex);
            } else if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                backIndex = cameraIndex;
                Log.d("pepe", "===> backIndex = " + backIndex);
            }
        }
        currentCameraType = type;
        if (type == FRONT && frontIndex != -1) {
            camera = Camera.open(frontIndex);
        } else if (type == BACK && backIndex != -1) {
            camera = Camera.open(backIndex);
        }
        //设置摄像头镜像成像
        camera.setDisplayOrientation(cameraOrientation);
        try {
            camera.setPreviewDisplay(mSurfaceHolder);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("pepe", "===> setPreviewDisplay error : " + e);
        }
        camera.startPreview();
        return camera;
    }

    /**
     * 关闭摄像头
     */
    public void closeCamera() {
        Log.d("pepe", "===> closeCamera");
        if (camera == null) {
            Log.d("pepe", "mCamera == null Exception");
            return;
        }
        try {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("pepe", "===> stopPreview error : " + e);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change:
                if(!isRecording){
                    changeCamera();
                }
                break;
            case R.id.picture:
                if(!isRecording){
                    tackPicture();
                }
                break;
            case R.id.record:
                if (isRecording) {
                    stopRecord();
                    openCamera(BACK);
                } else {
                    startRecord();
                }
                break;
            default:
                break;
        }
    }

    private void changeCamera() {
        camera.stopPreview();
        camera.release();
        if (currentCameraType == FRONT) {
            camera = openCamera(BACK);
        } else if (currentCameraType == BACK) {
            camera = openCamera(FRONT);
        }
        try {
            camera.setPreviewDisplay(surfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    private void tackPicture() {
        //自动对焦后拍照
        camera.autoFocus(autoFocusCallback);
    }

    /**
     * 自动对焦 对焦成功后 就进行拍照
     */
    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {

                //设置参数，并拍照
//                Camera.Parameters params = camera.getParameters();
//                params.setPictureFormat(PixelFormat.JPEG);//图片格式
//                params.setPreviewSize(800, 480);//图片大小
//                camera.setParameters(params);//将参数设置到我的camera

                // /对焦成功
                camera.takePicture(new Camera.ShutterCallback() {
                    //按下快门
                    @Override
                    public void onShutter() {
                        //按下快门瞬间的操作
                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {//是否保存原始图片的信息

                    }
                }, pictureCallback);
            }
        }
    };

    /**
     * 获取图片
     */
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("pepe", "PictureCallback onPictureTaken");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            //保存图片到系统相册
            saveImageToGallery(bitmap);
            startPreview();
        }
    };

    /**
     * 保存在相册
     */
    private void saveImageToGallery(Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Daocao");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();
            //recycleBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
                , Uri.parse("file://" + file.getAbsolutePath())));
    }

    private void startPreview() {
        camera.startPreview();
    }

    private boolean isRecording = false;
    private MediaRecorder mRecorder;//音视频录制类

    /**
     * 开始录制
     */
    public void startRecord() {
        Log.d("pepe", "===> startRecord");
        if (mRecorder == null) {
            mRecorder = new MediaRecorder(); // 创建MediaRecorder
        }
        camera.unlock();
        mRecorder.setCamera(camera);
        try {
            // 设置音频采集方式
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            // 设置音频采集方式
//            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置视频的采集方式
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            //设置文件的输出格式
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//aac_adif， aac_adts， output_format_rtp_avp， output_format_mpeg2ts ，webm
            //设置audio的编码格式
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //设置video的编码格式
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //设置录制的视频编码比特率
            mRecorder.setVideoEncodingBitRate(1024 * 1024);
            //设置录制的视频帧率,注意文档的说明:
            mRecorder.setVideoFrameRate(30);
            //设置要捕获的视频的宽度和高度
            mSurfaceHolder.setFixedSize(320, 240);//最高只能设置640x480
            mRecorder.setVideoSize(320, 240);//最高只能设置640x480
            //设置记录会话的最大持续时间（毫秒）
            mRecorder.setMaxDuration(5 * 60 * 1000);
            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            File dir = new File(Environment.getExternalStorageDirectory(), "Daocao");
            ///storage/822A-22BD/movie/184.mp4
            String time = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(System.currentTimeMillis());
            if (!dir.exists()) {
                dir.mkdir();
            }
            String path = dir + "/" + time + ".mp4";
            //设置输出文件的路径
            mRecorder.setOutputFile(path);
            mRecorder.setOnInfoListener(this);
            mRecorder.setOnErrorListener(this);

            //准备录制
            mRecorder.prepare();
            //开始录制
            mRecorder.start();
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        Log.d("pepe", "===> stopRecord");
        if (isRecording) {
            releaseMediaRecorder();
            camera.lock();
            closeCamera();
            isRecording = false;
        }
    }

    /**
     * 释放MediaRecorder
     */
    private void releaseMediaRecorder() {
        try {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int what, int extra) {
        Log.d("pepe", "===>  onError");
        Log.d("pepe", "===>  what = " + what + "  extra = " + extra);
        stopRecord();
        continueRecord();
    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
        Log.d("pepe", "===>  onInfo");
        Log.d("pepe", "===>  what = " + what + "  extra = " + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stopRecord();
            continueRecord();
        }
    }

    /**
     * 继续录制
     */
    private void continueRecord() {
        Log.d("pepe", "===> continueRecord");
        startRecord();
    }
}

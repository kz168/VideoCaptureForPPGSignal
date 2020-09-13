/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2video;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
//import com.android.volley.error.VolleyError;
//import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.android.camera2video.motiondetect.detection.IMotionDetection;
import com.example.android.camera2video.motiondetect.detection.RgbMotionDetection;
import com.example.android.camera2video.util.ImageUtil;
import com.example.android.camera2video.util.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.support.v4.content.ContextCompat.getSystemService;

public class Camera2VideoFragment extends Fragment
        implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback {

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    private static final String TAG = "Camera2VideoFragment";
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * Button to record video
     */
    private Button mButtonVideo;

    private Button flashButton;

    private Button submitData;

    private Button settings;

    private EditText nameText;
    private EditText emailText;
    private EditText phoneText;
    private EditText addressText;
    private Spinner gender;
    private EditText SubId;

    private Button cancelButton;
    private ProgressDialog progressDialog;
    private static HashMap<String,String> userData;

    private CheckBox rememberMeCheckBox;
    private SharedPreferences mSharedPrefrences;
    private SharedPreferences.Editor mEditor;
    private Chronometer chronometer;
    private ImageView imageToShow1;
    private ImageView imageToShow2;
    private ImageView imageToShow3;
    private ImageButton canButton;
    private TextView hbLevelText;
    private long stopTime = 0;
    RgbMotionDetection detector = new RgbMotionDetection();
    private boolean motionDetected = false;
    private CheckBox motionDetect;
    private CheckBox displayHgb;
    private boolean detectMotion;
    private EditText differentPixels;
    private EditText differenceInPixels;
    private EditText globaldifferentPixels;
    private EditText globaldifferenceInPixels;
    private Button setButton;
    private static boolean firstFrame = true;


    /**
     * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;


    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;

    private RequestQueue mRequestQueue;

    private ImageReader mImageReader;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);


    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private Integer mSensorOrientation;
    private String mNextVideoAbsolutePath;
    private CaptureRequest.Builder mPreviewBuilder;

    public static Camera2VideoFragment newInstance() {
        return new Camera2VideoFragment();
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 16 / 9 && size.getHeight() == 1080) {
                return size;
            }

//            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
//                return size;
//            }
        }

        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_video, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mButtonVideo = (Button) view.findViewById(R.id.video);
        chronometer = (Chronometer)view.findViewById(R.id.chronometer);
        progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        settings = (Button) view.findViewById(R.id.settings);

        mButtonVideo.setOnClickListener(this);
        mRequestQueue = Volley.newRequestQueue(getActivity());

        flashButton = view.findViewById(R.id.flash);
        flashButton.setOnClickListener(this);
        settings.setOnClickListener(this);

        userData = new HashMap<>();

        //view.findViewById(R.id.info).setOnClickListener(this);

        flashButton.setText(PreferenceUtil.getInstance(getContext()).isFlashOn() ? "Flash Off" : "Flash On");
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video: {
                if (mIsRecordingVideo) {
                    stopTime = 0;
                    chronometer.stop();
                    stopRecordingVideo();
                } else {
                    showDialog();
                    //startRecordingVideo();
                }
                break;
            }
            case R.id.info: {
                Activity activity = getActivity();
                if (null != activity) {
                    new AlertDialog.Builder(activity)
                            .setMessage(R.string.intro_message)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
                break;
            }
            case R.id.flash: {
                boolean isFlashOn = PreferenceUtil.getInstance(getContext()).isFlashOn();
                flashButton.setText(!isFlashOn ? "Flash Off" : "Flash On");
                PreferenceUtil.getInstance(getContext()).setFlashSettings(!isFlashOn);

                startPreview();
            }
               break;
            case R.id.settings: {
                showSettings();
            }
               break;
        }
    }

    private void turnFlashOn(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            String mCameraId = null;
            try {
                for (String camID : mCameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(camID);
                    int lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (lensFacing == CameraCharacteristics.LENS_FACING_BACK && cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                        mCameraId = camID;
                        break;
                    }
                }
                if (mCameraId != null) {
                    mCameraManager.setTorchMode(mCameraId, true);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void turnFlashOff(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            String mCameraId = null;
            try {
                for (String camID : mCameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(camID);
                    int lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (lensFacing == CameraCharacteristics.LENS_FACING_BACK && cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                        mCameraId = camID;
                        break;
                    }
                }
                if (mCameraId != null) {
                    mCameraManager.setTorchMode(mCameraId, false);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void showSettings()
    {
        AlertDialog.Builder alert;

        alert = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog_Alert);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.settings_data,null);

        setButton = (Button) view.findViewById(R.id.setBtn);
//        emailText = (EditText) view.findViewById(R.id.user_email);
//        phoneText = (EditText) view.findViewById(R.id.user_phone);
//        addressText = (EditText) view.findViewById(R.id.user_address);
        cancelButton = (Button) view.findViewById(R.id.cancelBtn);
        motionDetect = (CheckBox) view.findViewById(R.id.motionDetectionCheckBox);
        differentPixels = (EditText) view.findViewById(R.id.different_pixels);
        differenceInPixels = (EditText) view.findViewById(R.id.maxDifferenceValue);
        globaldifferentPixels = (EditText) view.findViewById(R.id.global_different_pixels);
        globaldifferenceInPixels = (EditText) view.findViewById(R.id.maxGlobalDifferenceValue);
        displayHgb = (CheckBox) view.findViewById(R.id.displayPPGCheckBox);

        mSharedPrefrences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPrefrences.edit();

        alert.setView(view);

        alert.setCancelable(false);


        final AlertDialog dialog = alert.create();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.show();

        checkSettings();

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(motionDetect.isChecked()){
                    mEditor.putString("different_Pixels",differentPixels.getText().toString());
                    mEditor.commit();

                    mEditor.putString("difference_In_Pixels",differenceInPixels.getText().toString());
                    mEditor.commit();

                    mEditor.putString("global_different_Pixels",globaldifferentPixels.getText().toString());
                    mEditor.commit();

                    mEditor.putString("global_difference_In_Pixels",globaldifferenceInPixels.getText().toString());
                    mEditor.commit();

                    mEditor.putString("motion_Detect","True");
                    mEditor.commit();

                }else{
                    mEditor.putString("motion_Detect","False");
                    mEditor.commit();
                }

                if(displayHgb.isChecked()){
                    mEditor.putString("display_PPG","True");
                    mEditor.commit();
                }else{
                    mEditor.putString("display_PPG","False");
                    mEditor.commit();
                }


                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    private void checkSettings(){

        String checkbox = mSharedPrefrences.getString("motion_Detect","False");

        String ppgcheckbox = mSharedPrefrences.getString("display_PPG","False");

        String different_pixels = mSharedPrefrences.getString("different_Pixels","");

        String difference_In_Pixels = mSharedPrefrences.getString("difference_In_Pixels","");

        String global_different_pixels = mSharedPrefrences.getString("global_different_Pixels","");

        String global_difference_In_Pixels = mSharedPrefrences.getString("global_difference_In_Pixels","");


        differentPixels.setText(different_pixels);
        differenceInPixels.setText(difference_In_Pixels);

        globaldifferentPixels.setText(global_different_pixels);
        globaldifferenceInPixels.setText(global_difference_In_Pixels);

        if(checkbox.equals("True")){
            motionDetect.setChecked(true);
        }else{
            motionDetect.setChecked(false);
        }

        if(ppgcheckbox.equals("True")){
            displayHgb.setChecked(true);
        }else{
            displayHgb.setChecked(false);
        }

    }

    private void showDialog()
    {
        AlertDialog.Builder alert;

        alert = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Dialog_Alert);

         LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

         View view = inflater.inflate(R.layout.user_data,null);

        submitData = (Button) view.findViewById(R.id.submitBtn);
//        nameText = (EditText) view.findViewById(R.id.user_name);
//        gender = (Spinner) view.findViewById(R.id.genderSelection);
          SubId = (EditText) view.findViewById(R.id.sub_id);
//        emailText = (EditText) view.findViewById(R.id.user_email);
//        phoneText = (EditText) view.findViewById(R.id.user_phone);
//        addressText = (EditText) view.findViewById(R.id.user_address);
        cancelButton = (Button) view.findViewById(R.id.cancelBtn);
        rememberMeCheckBox = (CheckBox) view.findViewById(R.id.rememberMecheckbox);

        mSharedPrefrences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEditor = mSharedPrefrences.edit();

         alert.setView(view);

         alert.setCancelable(false);


         final AlertDialog dialog = alert.create();
         dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         dialog.show();

        checkRememberMe();

        submitData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(getActivity(),nameText.getText().toString(),Toast.LENGTH_LONG).show();
                if(rememberMeCheckBox.isChecked()){
                    mEditor.putString("rememberChecked","True");
                    mEditor.commit();

                    mEditor.putString("sub_ID",SubId.getText().toString());
                    mEditor.commit();

//                    mEditor.putInt("gender",gender.getSelectedItemPosition());
//                    mEditor.commit();

//                    mEditor.putString("user_email",emailText.getText().toString());
//                    mEditor.commit();
//
//                    mEditor.putString("user_phone",phoneText.getText().toString());
//                    mEditor.commit();
//
//                    mEditor.putString("user_address",addressText.getText().toString());
//                    mEditor.commit();

                }else{
                    mEditor.putString("rememberChecked","False");
                    mEditor.commit();

                    mEditor.putString("sub_ID","");
                    mEditor.commit();

//                    mEditor.putInt("gender",0);
//                    mEditor.commit();

//                    mEditor.putString("user_email","");
//                    mEditor.commit();
//
//                    mEditor.putString("user_phone","");
//                    mEditor.commit();
//
//                    mEditor.putString("user_address","");
//                    mEditor.commit();
                }

                userData.put("sub_ID",SubId.getText().toString());
//                userData.put("gender",gender.getSelectedItem().toString());

//                userData.put("user_email",emailText.getText().toString());
//
//                userData.put("user_phone",phoneText.getText().toString());
//
//                userData.put("user_address",addressText.getText().toString());
                chronometer.setBase(SystemClock.elapsedRealtime() + stopTime);
                chronometer.start();
                firstFrame = true;
                startRecordingVideo();
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


    }
    private void checkRememberMe(){
        String checkbox = mSharedPrefrences.getString("rememberChecked","False");
//        String name = mSharedPrefrences.getString("user_name","");
        String sub_ID = mSharedPrefrences.getString("sub_ID","");
//        int genderSelection = mSharedPrefrences.getInt("gender",0);
//        String email = mSharedPrefrences.getString("user_email","");
//        String phone = mSharedPrefrences.getString("user_phone","");
//        String address = mSharedPrefrences.getString("user_address","");

        SubId.setText(sub_ID);
//        gender.setSelection(genderSelection);

//        emailText.setText(email);
//        phoneText.setText(phone);
//        addressText.setText(address);

        if(checkbox.equals("True")){
            rememberMeCheckBox.setChecked(true);
        }else{
            rememberMeCheckBox.setChecked(false);
        }

    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param permissions The permissions your app wants to request.
     * @return Whether you can show permission rationale UI.
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (FragmentCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requests permissions needed for recording video.
     */
    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance(getString(R.string.permission_request))
                                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                        break;
                    }
                }
            } else {
                ErrorDialog.newInstance(getString(R.string.permission_request))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    @SuppressWarnings("MissingPermission")
    private void openCamera(int width, int height) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        }
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

            //Setting the Preview size to standard 720p for lower Screen size devices
//            Size previewAspectRatio = new Size(1024,768);

            Size previewAspectRatio = new Size(1920,1080);
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, previewAspectRatio);

            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            configureTransform(width, height);
            mMediaRecorder = new MediaRecorder();
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Start the camera preview.
     */
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            final Activity activity = getActivity();
            if (null == activity || activity.isFinishing()) {
                return;
            }
            List<Surface> surfaces = new ArrayList<>();
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            if (PreferenceUtil.getInstance(getContext()).isFlashOn()) {
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            } else {
              mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
          }

            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//
            Range<Integer>[] fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);



            Log.d("FPSRANGESPREVIEW",Arrays.toString(fpsRanges));
            Range<Integer> fpsRate = new Range<>(30,30);

            for(Range<Integer> fpsRange : fpsRanges){
                if(fpsRange.getUpper() == 30 && fpsRange.getLower() == 30){
                    fpsRate = fpsRange;
                }
            }
            Log.d("FPSRATE",fpsRate.getUpper().toString() + ',' + fpsRate.getLower().toString());
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,fpsRate);
//
//            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE,
//                    CaptureRequest.CONTROL_AWB_MODE_OFF);

            mCameraDevice.createCaptureSession(surfaces,
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = getActivity();
                            if (null != activity) {
                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            try {
                long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
                Log.d("Time",Long.toString(seconds));

                if (mIsRecordingVideo && seconds > 10) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopTime = 0;
                            chronometer.stop();
                            stopRecordingVideo();
                        }
                    });


                }
                image = reader.acquireNextImage();

                int[] rgbData = ImageUtil.getRgbData(image);

                if(firstFrame){
//                    Log.d("RGBArray", Arrays.toString(rgbData));
//                    Log.d("ArraySize",Integer.toString(rgbData.length));
                    int arraySize = 0;
                    int[] RedValueArray = new int[rgbData.length];
                    int[] BlueValueArray = new int[rgbData.length];
                    int[] GreenValueArray = new int[rgbData.length];
                    int sumRed = 0;
                    int sumGreen = 0;
                    int sumBlue = 0;
                    int pix = 0;
                    int temp_R = 0;
                    int temp_G = 0;
                    int temp_B = 0;
                    for(int i=0 ;i<rgbData.length;i++){

                        pix = (0xff & rgbData[i]);
                        temp_R = rgbData[i] >> 16 & 0x0ff;
                        temp_G = rgbData[i] >> 8 & 0x0ff;
                        temp_B = rgbData[i] >> 0 & 0x0ff;

                        if (pix < 0) pix = 0;
                        if (pix > 255) pix = 255;

                        RedValueArray[i] = temp_R;
                        sumRed = sumRed + temp_R;

                        GreenValueArray[i] = temp_G;
                        sumGreen =  sumGreen + temp_G;

                        BlueValueArray[i] = temp_B;
                        sumBlue = sumBlue + temp_B;

                    }

                    arraySize = RedValueArray.length;

                    int averageRed = sumRed/arraySize;
                    int averageGreen = sumGreen/arraySize;
                    int averageBlue = sumBlue/arraySize;

                    Log.d("RedArrayAverage",Integer.toString(averageRed));
                    Log.d("RedArrayValue",Arrays.toString(RedValueArray));

                    Log.d("GreenArrayAverage",Integer.toString(averageGreen));
                    Log.d("GreenArrayValue",Arrays.toString(GreenValueArray));

                    Log.d("BlueArrayAverage",Integer.toString(averageBlue));
                    Log.d("BlueArrayValue",Arrays.toString(BlueValueArray));
                    firstFrame = false;

                    if(averageRed < 80){
                        if (mIsRecordingVideo) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"Place your finger properly",Toast.LENGTH_SHORT).show();
                                    stopTime = 0;
                                    chronometer.stop();
                                    motionDetected = true;
                                    stopRecordingVideo();
                                }
                            });


                        }
                    }

                    if(averageGreen > 70 || averageBlue > 70){

                        if (mIsRecordingVideo) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"Place your finger properly",Toast.LENGTH_SHORT).show();
                                    stopTime = 0;
                                    chronometer.stop();
                                    motionDetected = true;
                                    stopRecordingVideo();
                                }
                            });


                        }
                    }
                }



                mSharedPrefrences = PreferenceManager.getDefaultSharedPreferences(getContext());
                String checkbox = mSharedPrefrences.getString("motion_Detect","False");
//
                if(checkbox.equals("True")){
                    String differentPixels = mSharedPrefrences.getString("different_Pixels","").isEmpty() ? "200" : mSharedPrefrences.getString("different_Pixels","");
                    String differenceInPixels = mSharedPrefrences.getString("difference_In_Pixels","").isEmpty() ? "40" : mSharedPrefrences.getString("difference_In_Pixels","");

                    String globaldifferentPixels = mSharedPrefrences.getString("global_different_Pixels","").isEmpty() ? "200" :  mSharedPrefrences.getString("global_different_Pixels","");
                    String globaldifferenceInPixels = mSharedPrefrences.getString("global_difference_In_Pixels","").isEmpty() ? "40" : mSharedPrefrences.getString("global_difference_In_Pixels","");



                    detector.setmPixelThreshold(Integer.parseInt(differenceInPixels));
                    detector.setmThreshold(Integer.parseInt(differentPixels));
                    detector.setGlobalPixelThreshold(Integer.parseInt(globaldifferenceInPixels));
                    detector.setGlobalThreshhold(Integer.parseInt(globaldifferentPixels));

                    boolean detected = detector.detect(rgbData, image.getWidth(), image.getHeight());

                    if(detected){
                        Log.d("Image","Motion Detected");

                        if (mIsRecordingVideo) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"Motion Detected",Toast.LENGTH_SHORT).show();
                                    stopTime = 0;
                                    chronometer.stop();
                                    motionDetected = true;
                                    stopRecordingVideo();
                                }
                            });


                        }
                    }
//                    if (image != null) {
//                        Log.d("Image", "Image Acquired");
//                        image.close();
//                    }
                }

                if (image != null) {
                    Log.d("Image", "Image Acquired");
                    image.close();
                }


            } catch (Exception e) {
                Log.d("Image", e.getMessage());
            }
        }
    };

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(getActivity());
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(60);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
//        mMediaRecorder.setVideoSize(1920,1080);
        //Log.d(TAG,"Video size : "+mVideoSize.getWidth()+"X"+mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
    }

    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }

    private void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

//            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE,
//                    CaptureRequest.CONTROL_AWB_MODE_OFF);

            if (PreferenceUtil.getInstance(getContext()).isFlashOn()) {
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }

            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            final Activity activity = getActivity();
            if (null == activity || activity.isFinishing()) {
                return;
            }

            CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Range<Integer>[] fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

            Log.d("FPSRANGESVIDEO",Arrays.toString(fpsRanges));
            Range<Integer> fpsRate = new Range<>(30,30);

            for(Range<Integer> fpsRange : fpsRanges){
                if(fpsRange.getUpper() == 60 && fpsRange.getLower() == 60){
                    fpsRate = fpsRange;
                }
            }

            Log.d("FPSRATE",fpsRate.getUpper().toString() + ',' + fpsRate.getLower().toString());

            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,fpsRate);

            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());

            detector.initialize();
            Log.d(TAG,"Detector Initialized");

            mImageReader = ImageReader.newInstance(largest.getWidth() / 4,
                    largest.getHeight() / 4, ImageFormat.YUV_420_888, 2);

            mImageReader.setOnImageAvailableListener(
                    mOnImageAvailableListener, mBackgroundHandler);

            Surface mImageSurface = mImageReader.getSurface();
            surfaces.add(mImageSurface);
            mPreviewBuilder.addTarget(mImageSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI
                            mButtonVideo.setText(R.string.stop);
                            mIsRecordingVideo = true;

                            // Start recording
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
        mButtonVideo.setText(R.string.record);

        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

       if(!motionDetected) {
           Activity activity = getActivity();
           if (null != activity) {
//            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
//                    Toast.LENGTH_SHORT).show();
               Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
               String[] splitAbsolutePath = mNextVideoAbsolutePath.split("/");
               String directoryPath = "";
               int index = 1;
               while (index < (splitAbsolutePath.length - 1)) {
                   directoryPath = directoryPath + "/" + splitAbsolutePath[index];
                   index++;
               }
               mSharedPrefrences = PreferenceManager.getDefaultSharedPreferences(getContext());
               String checkbox = mSharedPrefrences.getString("display_PPG","False");

               if(checkbox.equals("True")){
                   uploadVideoFileShowOutput("http://192.168.0.149:8000/mobile-api/saveVideoHbLevel/", mNextVideoAbsolutePath, splitAbsolutePath[splitAbsolutePath.length - 1], directoryPath);
               }else{
                   uploadVideoFileSave("http://192.168.0.149:8000/mobile-api/saveVideo/", mNextVideoAbsolutePath, splitAbsolutePath[splitAbsolutePath.length - 1], directoryPath);
               }


           }
           mNextVideoAbsolutePath = null;
       }
        motionDetected = false;
        startPreview();

    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(parent, VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parent.getActivity().finish();
                                }
                            })
                    .create();
        }

    }

//    private void uploadVideoFile(String Url,String filePath){
//        SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, Url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d("Response", response);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d("Response", error.getMessage());
//            }
//        });
//        smr.addStringParam("param string", " data text");
//        smr.addFile("param file", filePath);
//        mRequestQueue.add(smr);
//    }

      private void uploadVideoFileShowOutput(String url, final String filePath, String fileName,final String directory){
              final byte[] inputData = fileToByteArray(filePath);
              final String fileNameInput = fileName;

              showProgress(true);

              VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                      new Response.Listener<NetworkResponse>() {
                          @Override
                          public void onResponse(NetworkResponse response) {
                              String HbLevel = response.headers.get("Hblevel");
                              Log.d("response",new String(response.data));

                              Toast.makeText(getActivity(), "Data Saved Successfully", Toast.LENGTH_SHORT).show();
                              List<String> ImagePaths = new ArrayList<>();
                              unzip(response.data,directory,ImagePaths);

                              if(!ImagePaths.isEmpty()){
//                                  Bitmap drawableBitmap = BitmapFactory.decodeByteArray(response.data,0,response.data.length);
                                  Bitmap drawableBitmap1 = BitmapFactory.decodeFile(ImagePaths.get(0));
                                  File deleteFile = new File(ImagePaths.get(0));

                                  if(deleteFile.exists()){
                                      deleteFile.delete();
                                  }

                                  Bitmap drawableBitmap2 = BitmapFactory.decodeFile(ImagePaths.get(1));
                                  File deleteFile2 = new File(ImagePaths.get(1));

                                  if(deleteFile2.exists()){
                                      deleteFile2.delete();
                                  }

                                  Bitmap drawableBitmap3 = BitmapFactory.decodeFile(ImagePaths.get(2));
                                  File deleteFile3 = new File(ImagePaths.get(2));

                                  if(deleteFile3.exists()){
                                      deleteFile3.delete();
                                  }

                                  onButtonShowPopupWindowClick(getView(),drawableBitmap1,drawableBitmap2,drawableBitmap3,HbLevel);
                              }

                              showProgress(false);
                          }
                      },
                      new Response.ErrorListener() {
                          @Override
                          public void onErrorResponse(VolleyError error) {
                              Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                              showProgress(false);
                          }
                      }) {

                  /*
                   * If you want to add more parameters with the image
                   * you can do it here
                   * here we have only one parameter with the image
                   * which is tags
                   * */
                  @Override
                  protected Map<String, String> getParams() throws AuthFailureError {
                      Map<String, String> params = new HashMap<>();
                      // params.put("tags", "ccccc");  add string parameters
                      params.put("Sub_ID",userData.get("sub_ID"));
//                      params.put("Gender",userData.get("gender"));
//                      params.put("Phone",userData.get("user_phone"));
//                      params.put("Email",userData.get("user_email"));
//                      params.put("Address",userData.get("user_address"));

                      return params;
                  }

                  /*
                   *pass files using below method
                   * */
                  @Override
                  protected Map<String, DataPart> getByteData() {
                      Map<String, DataPart> params = new HashMap<>();
                      params.put("file", new DataPart(fileNameInput ,inputData));
                      return params;
                  }
              };


              volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                      0,
                      DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                      DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

              mRequestQueue.add(volleyMultipartRequest);


      }

    private void uploadVideoFileSave(String url, final String filePath, String fileName,final String directory){
        final byte[] inputData = fileToByteArray(filePath);
        final String fileNameInput = fileName;

        showProgress(true);

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                       String responseString = new String(response.data);
                        try {
                            JSONObject responseObj = new JSONObject(responseString);
                            if(responseObj.getString("Status").equals("Ok") && responseObj.getString("code").equals("200")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setIcon(R.drawable.ic_baseline_assignment_turned_in_24);
                                builder.setTitle(R.string.response_title);
                                builder.setMessage(R.string.data_save_successfull)
                                        .setPositiveButton(R.string.positive_response, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                            }
                                        });
                                builder.show();
                            }else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setIcon(R.drawable.ic_cancel_black_24dp);
                                builder.setTitle(R.string.response_title);
                                builder.setMessage(R.string.data_save_unsuccessfull)
                                        .setPositiveButton(R.string.positive_response, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        });
                                builder.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        showProgress(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // params.put("tags", "ccccc");  add string parameters
                params.put("Sub_ID",userData.get("sub_ID"));
//                      params.put("Gender",userData.get("gender"));
//                      params.put("Phone",userData.get("user_phone"));
//                      params.put("Email",userData.get("user_email"));
//                      params.put("Address",userData.get("user_address"));

                return params;
            }

            /*
             *pass files using below method
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart(fileNameInput ,inputData));
                return params;
            }
        };


        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(volleyMultipartRequest);


    }

    public void onButtonShowPopupWindowClick(View view, Bitmap bitmapImage1,Bitmap bitmapImage2,Bitmap bitmapImage3,String hbLevel) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popout_image, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        imageToShow1 = (ImageView) popupView.findViewById(R.id.imagePop1);
        imageToShow2 = (ImageView) popupView.findViewById(R.id.imagePop2);
        imageToShow3 = (ImageView) popupView.findViewById(R.id.imagePop3);
        canButton = (ImageButton) popupView.findViewById(R.id.cancelButton);
        hbLevelText = (TextView) popupView.findViewById(R.id.hbLevelText);
        hbLevelText.setText("Hemoglobin Level : "+hbLevel);
        imageToShow1.setImageBitmap(bitmapImage1);
        imageToShow2.setImageBitmap(bitmapImage2);
    //  imageToShow3.setImageBitmap(bitmapImage3);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
//        popupView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                popupWindow.dismiss();
//                return true;
//            }
//        });

        canButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    private void showProgress(final boolean show) {

        if(show){
            progressDialog.setIndeterminate(true);
//            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage("Processing...");
            progressDialog.show();

        }else{
            progressDialog.dismiss();
        }


    }

      private byte[] fileToByteArray(String filePath)
      {
          File file = new File(filePath);

          FileInputStream fis = null;
          try {
              fis = new FileInputStream(file);
          } catch (FileNotFoundException e) {
              Toast.makeText(getActivity(),"File Not found",Toast.LENGTH_SHORT).show();
          }
          //System.out.println(file.exists() + "!!");
          //InputStream in = resource.openStream();
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          byte[] buf = new byte[1024];
          try {
              for (int readNum; (readNum = fis.read(buf)) != -1;) {
                  bos.write(buf, 0, readNum); //no doubt here is 0
                  //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
                  System.out.println("read " + readNum + " bytes,");
              }
          } catch (IOException ex) {
              Toast.makeText(getActivity(),"IOException",Toast.LENGTH_SHORT).show();
          }
          return bos.toByteArray();

      }

      private void unzip(byte[] bytes,String directory,List<String> ImagePaths)
      {
          ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(bytes));
          ZipEntry entry = null;
          while (true) {
              try {
                  if (!((entry = zipStream.getNextEntry()) != null)) break;
              } catch (IOException e) {
                  e.printStackTrace();
              }

              String entryName = entry.getName();


              File newFile = new File(directory + "/" + entryName);

              try {
                  newFile.createNewFile();
                  ImagePaths.add(directory + "/" + entryName);
              } catch (IOException e) {
                  e.printStackTrace();
              }


              FileOutputStream out = null;
              try {

                  out = new FileOutputStream(newFile);
              } catch (FileNotFoundException e) {
                  e.printStackTrace();
              }

              byte[] byteBuff = new byte[4096];
              int bytesRead = 0;
              try {
              while ((bytesRead = zipStream.read(byteBuff)) != -1)
              {
                  out.write(byteBuff, 0, bytesRead);
              }

                  out.close();
                  zipStream.closeEntry();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          try {
              zipStream.close();
          } catch (IOException e) {
              e.printStackTrace();
          }

      }


}

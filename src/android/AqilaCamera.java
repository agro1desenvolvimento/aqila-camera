package br.com.aqila.camera;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.commonsware.cwac.cam2.CameraActivity;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.FocusMode;
import com.commonsware.cwac.cam2.ZoomStyle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.OutputStream;
import java.util.Date;

public class AqilaCamera extends CordovaPlugin {
    private static final String TAG = "aqila-camera";
    public static final int REQUEST_CAMERA = 1338;
    public static final int REQUEST_PERMISSION = 1338;
    protected final static String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

    private CallbackContext callbackContext;
    private String file;
    private String dstPath;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, action + " " + args.length());

        this.file = null;
        this.callbackContext = null;

        if (action.equals("takePicture")) {
            this.callbackContext = callbackContext;

            String filepath = cordova.getActivity().getApplicationContext().getFilesDir().getAbsolutePath();
            String dstPath = "Aqila/fotos/";
            if (args.length() > 0 && args.getString(0) != null && !args.getString(0).isEmpty()) {
                dstPath = args.getString(0);
            }
            File folder = new File(filepath, dstPath);
            file = folder.getAbsolutePath() + "/" + new Date().getTime() + ".jpg";

            Intent cameraIntent = new CameraActivity.IntentBuilder(cordova.getActivity())
                    .skipConfirm()
                    .facing(Facing.BACK)
                    .focusMode(FocusMode.CONTINUOUS)
                    .facingExactMatch()
                    .zoomStyle(ZoomStyle.PINCH)
                    .flashMode(FlashMode.OFF)
                    .to(new File(file))
                    .requestPermissions()
                    .skipOrientationNormalization()
                    .updateMediaStore()
                    .build();

            this.cordova.startActivityForResult(this, cameraIntent, REQUEST_CAMERA);

            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);

            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CAMERA) {
            if (file != null && new File(this.file).isFile()) {
                try {
                    processImage();
                    callbackContext.success(file);
                } catch (Exception e) {
                    callbackContext.error("Falha ao processar imagem");
                    Log.e(TAG, "Falha ao procesar imagem", e);
                }
            } else {
                callbackContext.error("");
            }
        }
    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        this.file = state.getString("file");
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putString("file", this.file);
        return state;
    }

    private void processImage() throws Exception {
        OutputStream os = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            Bitmap unscaledBitmap = BitmapFactory.decodeFile(file, options);
            options.inSampleSize = calculateSampleSize(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), 1280, 720);
            Bitmap scaledBitmap = BitmapFactory.decodeFile(file, options);
            os = this.cordova.getActivity().getContentResolver().openOutputStream(Uri.fromFile(new File(file)));
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, os);
        } finally {
            os.close();
        }
    }

    private static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        final float srcAspect = (float) srcWidth / (float) srcHeight;
        final float dstAspect = (float) dstWidth / (float) dstHeight;

        if (srcAspect > dstAspect) {
            return srcWidth / dstWidth;
        } else {
            return srcHeight / dstHeight;
        }
    }
}
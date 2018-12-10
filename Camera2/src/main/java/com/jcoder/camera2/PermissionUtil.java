package com.jcoder.camera2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * @author wang
 * @date 2018/12/8.
 */

public class PermissionUtil {
    public static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static final int REQUEST_VIDEO_PERMISSIONS = 1;

    public static boolean hasPermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestVideoPermissions(final Context context) {
        if (shouldShowRequestPermissionRationale(context,VIDEO_PERMISSIONS)) {
            Log.d("pepe", "should request video permission");
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions( ((Activity)context), VIDEO_PERMISSIONS,
                                    REQUEST_VIDEO_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((Activity)context).finish();
                                }
                            })
                    .create();
            dialog.show();
        } else {
            ((Activity)context).requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
            Log.d("pepe", "not should request video permission");
//            ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    public static boolean shouldShowRequestPermissionRationale(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale( ((Activity)context), permission)) {
                return true;
            }
        }
        return false;
    }
}

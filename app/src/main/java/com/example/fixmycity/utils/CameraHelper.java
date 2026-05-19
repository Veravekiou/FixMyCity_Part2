package com.example.fixmycity.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraHelper {

    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;
    public static final int CAMERA_REQUEST_CODE = 1003;

    private final Activity activity;
    private Uri photoUri;

    public CameraHelper(Activity activity) {
        this.activity = activity;
    }

    // Δημιουργεί ένα προσωρινό αρχείο για την φωτογραφία
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "PHOTO_" + timeStamp + "_";

        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // Ανοίγει την κάμερα
    public Uri openCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(
                    activity,
                    activity.getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile
            );

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            activity.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

            return photoUri;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Επιστρέφει το URI της φωτογραφίας
    public Uri getPhotoUri() {
        return photoUri;
    }
}
package com.example.fixmycity.utils;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ImageUploadHelper {

    private final FirebaseStorage storage;

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    public ImageUploadHelper() {
        storage = FirebaseStorage.getInstance();
    }

    public void uploadImage(Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new Exception("No image selected"));
            return;
        }

        String fileName = "reports/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = storage.getReference().child(fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        callback.onSuccess(uri.toString()))
                                .addOnFailureListener(callback::onFailure))
                .addOnFailureListener(callback::onFailure);
    }
}
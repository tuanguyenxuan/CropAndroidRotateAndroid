package com.yalantis.ucrop.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;

public class MainActivity extends BaseActivity {

    private static final String TAG = "SampleActivity";

    private static final int REQUEST_SELECT_PICTURE = 0x01;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";

    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PICTURE) {
                final Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    startCropActivity(data.getData());
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    private void startCropActivity(Uri data) {
        UCrop uCrop = UCrop.of(data, Uri.fromFile(new File(getCacheDir(), "SloRNCropImage.png")));
        uCrop = basisConfig(uCrop);
        uCrop = advancedConfig(uCrop);

        uCrop.start(MainActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.startEditBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });
    }

    private void pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), REQUEST_SELECT_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            ResultActivity.startWithUri(MainActivity.this, resultUri);
        } else {
            Toast.makeText(MainActivity.this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            Toast.makeText(MainActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
        }
    }

    private UCrop basisConfig(@NonNull UCrop uCrop) {
        uCrop = uCrop.withAspectRatio(1, 1);
        return uCrop;
    }

    /**
     * Sometimes you want to adjust more options, it's done via {@link com.yalantis.ucrop.UCrop.Options} class.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(100);

        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(false);
        return uCrop.withOptions(options);
    }
}

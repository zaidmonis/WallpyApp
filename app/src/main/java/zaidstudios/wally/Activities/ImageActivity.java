package zaidstudios.wally.Activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.io.OutputStream;

import zaidstudios.wally.Helper.CheckConnection;
import zaidstudios.wally.Helper.GlideImageLoader;
import zaidstudios.wally.R;

public class ImageActivity extends AppCompatActivity {
    ImageView imageView, bImageView;
    TextView loadingTextView;
    ProgressBar progressBar;
    Button setButton, downloadButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_image);
        imageView = findViewById(R.id.fullImage);
        bImageView = findViewById(R.id.bImageView);
        loadingTextView = findViewById(R.id.loadingTextView);
        progressBar = findViewById(R.id.progress);

        setButton = findViewById(R.id.setButton);
        downloadButton = findViewById(R.id.downloadButton);
        backButton = findViewById(R.id.backButton);
        loadingTextView.setVisibility(View.VISIBLE);

        final String imageUrl = getIntent().getStringExtra("url");

        if (imageUrl != null && imageUrl.startsWith("http")) {
            // It's a remote URL
            final String wallUrl = convertURLsToOriginal(imageUrl);
            Glide.with(ImageActivity.this).load(imageUrl).into(bImageView);

            if (!CheckConnection.isOnline(this)) {
                Alerter.create(ImageActivity.this).setText("No Internet Connection!!!").setTitle("Oops!").setIcon(R.drawable.error).setDuration(7000).enableSwipeToDismiss().show();
            }

            RequestOptions options = new RequestOptions().priority(Priority.HIGH);

            GlideImageLoader gil = new GlideImageLoader(imageView, progressBar, loadingTextView, bImageView, this);
            gil.load(wallUrl, options);

            setButton.setOnClickListener(v -> {
                if (isStoragePermissionGranted()) {
                    downloadAndSetWallpaper(wallUrl);
                } else {
                    requestStoragePermission();
                }
            });

            downloadButton.setOnClickListener(v -> {
                if (isStoragePermissionGranted()) {
                    downloadAndSaveImage(wallUrl);
                } else {
                    requestStoragePermission();
                }
            });
        } else {
            // It's a local file path
            downloadButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            loadingTextView.setVisibility(View.GONE);
            bImageView.setVisibility(View.GONE);

            File localFile = new File(imageUrl);
            Glide.with(ImageActivity.this).load(localFile).into(imageView);

            setButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                Uri imageUri = Uri.fromFile(localFile);
                intent.setDataAndType(imageUri, "image/jpeg");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Set as:"));
            });
        }

        backButton.setOnClickListener(view -> onBackPressed());
    }

    private void downloadAndSetWallpaper(String wallUrl) {
        Glide.with(ImageActivity.this)
                .asBitmap()
                .load(wallUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        Uri imageUri = saveImageAndGetUri(resource, wallUrl);
                        if (imageUri != null) {
                            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setDataAndType(imageUri, "image/jpeg");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "Set as:"));
                        } else {
                            Toast.makeText(ImageActivity.this, "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void downloadAndSaveImage(String wallUrl) {
        Glide.with(ImageActivity.this)
                .asBitmap()
                .load(wallUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @NonNull Transition<? super Bitmap> transition) {
                        final Uri imageUri = saveImageAndGetUri(resource, wallUrl);
                        if (imageUri != null) {
                            Alerter.create(ImageActivity.this)
                                    .setTitle("Done")
                                    .setText("Image saved Successfully")
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setDataAndType(imageUri, "image/jpeg");
                                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                startActivity(intent);
                                            } catch (Exception e) {
                                                Toast.makeText(getApplicationContext(), "Unable to open image!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .enableSwipeToDismiss()
                                    .show();
                        } else {
                            Toast.makeText(ImageActivity.this, "Failed to save image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private Uri saveImageAndGetUri(Bitmap finalBitmap, String name) {
        String cleanName = name.replaceAll("https://i.imgur.com/", "");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, cleanName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "Wallpy");
        } else {
            File directory = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Wallpy");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, cleanName);
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                return uri;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            } else {
                return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }
        } else {
            return true; // Permissions are granted at install time on older versions
        }
    }

    private void requestStoragePermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        ActivityCompat.requestPermissions(this, permissions, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. You can now save images.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. You cannot save images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String convertURLsToOriginal(String url) {
        if (url == null) return "";
        url = url.replaceAll("h.jpg", ".jpg");
        url = url.replaceAll("h.png", ".png");
        url = url.replaceAll("h.jpeg", ".jpeg");
        url = url.replaceAll("m.jpg", ".jpg");
        url = url.replaceAll("m.png", ".png");
        url = url.replaceAll("m.jpeg", ".jpeg");
        return url;
    }
}

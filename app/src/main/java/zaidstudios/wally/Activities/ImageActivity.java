package zaidstudios.wally.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.io.FileOutputStream;

import zaidstudios.wally.Helper.CheckConnection;
import zaidstudios.wally.Helper.GlideImageLoader;
import zaidstudios.wally.Helper.MyScanService;
import zaidstudios.wally.R;

public class ImageActivity extends AppCompatActivity {
    ImageView imageView, bImageView;
    TextView loadingTextView;
    ProgressBar progressBar;
    Button setButton, downloadButton, backButton;
    //MainActivity mainActivity;
    int STORAGE_PERMISSION_CODE;
    File wallfile;
    ImageActivity img;

    File myDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle("");



        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());



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



        String root = Environment.getExternalStorageDirectory().toString();
        myDir = new File(root + "/Wallpy/");
        myDir.mkdirs();


        final String bimageURL = getIntent().getStringExtra("url");

        final String wallUrl = convertURLsToOriginal(bimageURL);


        String fname = wallUrl.replaceAll("https://i.imgur.com/", "");
            final File imageFile = new File(myDir, fname);
        if (imageFile.isFile()){
            Glide.with(ImageActivity.this).load(imageFile).into(bImageView);
            progressBar.setVisibility(View.GONE);
            loadingTextView.setVisibility(View.GONE);
        }
        else{
            Glide.with(ImageActivity.this).load(bimageURL).into(bImageView);


            if (!CheckConnection.isOnline(this)){
                Alerter.create(ImageActivity.this).setText("No Internet Connection!!!").setTitle("Oops!").setIcon(R.drawable.error).setDuration(7000).enableSwipeToDismiss().show();
            }



            RequestOptions options = new RequestOptions().priority(Priority.HIGH);

            GlideImageLoader gil = new GlideImageLoader(imageView, progressBar, loadingTextView, bImageView, this);
            gil.load(wallUrl,options);
            //loadingTextView.setVisibility(View.GONE);
            //loadingTextView.setText(String.valueOf(progressBar.getProgress()));





//            Glide.with(ImageActivity.this).load(wallUrl).listener(new RequestListener<Drawable>() {
//                @Override
//                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                    progressBar.setVisibility(View.GONE);
//                    Toast.makeText(ImageActivity.this, "Unable to Load!", Toast.LENGTH_SHORT).show();
//                    return false;
//                }
//
//                @Override
//                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                    bImageView.setVisibility(View.GONE);
//                    progressBar.setVisibility(View.GONE);
//                    loadingTextView.setVisibility(View.GONE);
//                    return false;
//                }
//            }).into(imageView);
        }



        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isReadStorageAllowed()){
                    if (imageFile.isFile()){
                        /*Intent intent = new Intent(ImageActivity.this, SetWallpaperActivity.class);
                        intent.putExtra("file", imageFile.getAbsolutePath());
                        startActivity(intent);*/
                        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                        intent.setDataAndType(Uri.fromFile(imageFile), "image/*");
                        startActivity(Intent.createChooser(intent, "Select Wallpaper"));
                    }
                    else {
                        Glide.with(ImageActivity.this)
                                .asBitmap()
                                .load(wallUrl)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                        try {
                                            wallfile = SaveImage(resource, wallUrl);
                                            /*Intent intent = new Intent(ImageActivity.this, SetWallpaperActivity.class);
                                            intent.putExtra("file", wallfile);
                                            startActivity(intent);*/
                                            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                                            intent.setDataAndType(Uri.fromFile(wallfile), "image/*");
                                            startActivity(Intent.createChooser(intent, "Select Wallpaper"));

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                }
                else{
                    requestStoragePermission();
                }
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (isReadStorageAllowed()) {
                    if (imageFile.isFile()) {
                        //Toast.makeText(ImageActivity.this, "Already Saved", Toast.LENGTH_SHORT).show();
                        Alerter.create(ImageActivity.this)
                                .setTitle("")
                                .setText("Image Already saved")
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try{
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setDataAndType(Uri.fromFile(imageFile), "image/jpg");
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        } catch(Exception e){
                                            Toast.makeText(getApplicationContext(), "Unable to open image!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .enableSwipeToDismiss()
                                .setBackgroundColorRes(R.color.theft2)
                                .show();
                    } else {
                        Glide.with(ImageActivity.this)
                                .asBitmap()
                                .load(wallUrl)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                        try {
                                            wallfile = SaveImage(resource, wallUrl);
                                            //Toast.makeText(ImageActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                            Alerter.create(ImageActivity.this)
                                                    .setTitle("Done")
                                                    .setText("Image saved Successfully")
                                                    .setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            try{
                                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                                intent.setDataAndType(Uri.fromFile(wallfile), "image/jpg");
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);
                                                            } catch(Exception e){
                                                                Toast.makeText(getApplicationContext(), "Unable to open image!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    })
                                                    .enableSwipeToDismiss()
                                                    .show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                }
                else{
                    requestStoragePermission();
                }
            }
        });
    }










    private File SaveImage(Bitmap finalBitmap, String name) {
        name = name.replaceAll("https://i.imgur.com/", "");
        String fname = name;
        File imageFile = new File (myDir, fname);
        if (!(imageFile.exists ())){
            try {
                FileOutputStream out = new FileOutputStream(imageFile);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        startService(new Intent(ImageActivity.this, MyScanService.class).putExtra("path", imageFile.toString()));
        return imageFile;
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        //super.onWindowFocusChanged(hasFocus);
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
    public boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }
    //Requesting permission
    public void requestStoragePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
    }
    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == STORAGE_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Permission Granted
            }else{
                //permission is not granted, Requesting again
                Toast.makeText(this, "Please click Allow to save Wallpapers in Device!", Toast.LENGTH_SHORT).show();
                //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
            }
        }
    }


    public String convertURLsToOriginal(String url){
        url = url.replaceAll("h.jpg", ".jpg");
        url = url.replaceAll("h.png", ".png");
        url = url.replaceAll("h.jpeg", ".jpeg");
        url = url.replaceAll("m.jpg", ".jpg");
        url = url.replaceAll("m.png", ".png");
        url = url.replaceAll("m.jpeg", ".jpeg");
        return url;

    }
}

package zaidstudios.wally.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import zaidstudios.wally.R;

public class SetWallpaperActivity extends AppCompatActivity {

    CropImageView cropImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wallpaper);
        String fileName = getIntent().getStringExtra("file");
        Toast.makeText(this, ""+fileName, Toast.LENGTH_SHORT).show();
        File file = new File(fileName);
        if (file.isFile()){
            Toast.makeText(this, "isFile", Toast.LENGTH_SHORT).show();


            InputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize  = 1;
            bitmapOptions.inJustDecodeBounds = false;
            Bitmap wallpaperBitmap = BitmapFactory.decodeStream(fileInputStream, null, bitmapOptions);

            cropImageView = this.findViewById(R.id.CropImageView);
            cropImageView.setImageBitmap(wallpaperBitmap);
            cropImageView.setFixedAspectRatio(true);
            cropImageView.setAspectRatio(9 , 16);
        }
    }
}

package zaidstudios.wally.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.Menu;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import zaidstudios.wally.Helper.CheckConnection;
import zaidstudios.wally.Helper.ImageListAdapter;
import zaidstudios.wally.ImageURLs.URLStrings;
import zaidstudios.wally.Helper.MyScanService;
import zaidstudios.wally.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, URLStrings {
    GridView gridView;
    ImageView imageView;
    URLStrings urlStrings;
    int STORAGE_PERMISSION_CODE;
    String currentVersion = "0";

    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private ProgressBar progressBar;
    boolean loadHDThumb;
    boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //loadHDThumb = false;
        doubleBackToExitPressedOnce = false;


        loadHDThumb = getSharedPreferences("isHDThumb", MODE_PRIVATE).getBoolean("HDThumb", false);

        gridView = findViewById(R.id.gridView);
        progressBar = findViewById(R.id.progress);
        imageView = findViewById(R.id.imageView);
        gridView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        gridView.setVisibility(View.INVISIBLE);
        //NavigationView navigationView;

        if (isReadStorageAllowed()) {
            //Already have the Permission

        } else {
            //app don't have permission, asking for the permission
            requestStoragePermission();
        }

        checkForUpdates();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        //navigationView.callOnClick();
        navigationView.setNavigationItemSelectedListener(this);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));

    }


    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public List<String> replace(List<String> list) {
        String oldStr = "";
        String someString = "m.jpg";
        String otherString = "l.jpg";
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains(someString)) {
                oldStr = list.get(i).replace(someString, otherString);
                list.set(i, oldStr);
            }
        }
        return list;
    }


    public void loadDownloadedWallpapers() {
        String path = Environment.getExternalStorageDirectory().toString() + "/Wallpy";
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            String[] downloadedFiles = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                downloadedFiles[i] = files[i].toString();
            }
            List<String> downloadedWallpapers = Arrays.asList(downloadedFiles);
            progressBar.setVisibility(View.INVISIBLE);
            loadGrid(downloadedWallpapers);
        }
    }


    public void loadUrlsFromFirebase(String category) {
        progressBar.setVisibility(View.VISIBLE);
        gridView.setVisibility(View.INVISIBLE);
        if (!CheckConnection.isOnline(this)) {
            Alerter.create(MainActivity.this).setText("No Internet Connection!!!").setTitle("Oops!").setIcon(R.drawable.error).setDuration(7000).enableSwipeToDismiss().show();
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(category);
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //mStrings.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                }

                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                };

                List<String> yourStringArray = dataSnapshot.getValue(t);

                if (loadHDThumb) {
                    yourStringArray = convertURLsToHD(yourStringArray);
                }
                System.out.println(yourStringArray);
                Collections.reverse(yourStringArray);
                //List<String> newArray = replace(yourStringArray);

                gridView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                loadGrid(yourStringArray);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        //stopService(new Intent(MainActivity.this, MyScanService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, MyScanService.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            loadUrlsFromFirebase("All");
            setTitle("New");
        }
        if (id == R.id.downlaods) {
            loadDownloadedWallpapers();
            setTitle("Downloads");
        }
        if (id == R.id.nav_anime) {
            // Handle the camera action
            //Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
            loadUrlsFromFirebase("Anime");
            setTitle("Anime");
        } else if (id == R.id.nav_girls) {
            loadUrlsFromFirebase("Girls");
            setTitle("Girls");
        } else if (id == R.id.nav_iphone) {
            loadUrlsFromFirebase("iPhone");
            setTitle("iPhone");
        } else if (id == R.id.amoled) {
            loadUrlsFromFirebase("Amoled");
            setTitle("Black");
        } else if (id == R.id.sports) {
            loadUrlsFromFirebase("Sports");
            setTitle("Sports");
        } else if (id == R.id.nature) {
            loadUrlsFromFirebase("Nature");
            setTitle("Nature");
        } else if (id == R.id.nav_3d) {
            loadUrlsFromFirebase("3D");
            setTitle("3D");
        } else if (id == R.id.animals) {
            loadUrlsFromFirebase("Animals");
            setTitle("Animals");
        } else if (id == R.id.nav_city) {
            loadUrlsFromFirebase("CityScapes");
            setTitle("CityScapes");
        } else if (id == R.id.abstracte) {
            loadUrlsFromFirebase("Abstract");
            setTitle("Abstract");
        } else if (id == R.id.nav_cars) {
            loadUrlsFromFirebase("Cars");
            setTitle("Cars");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void loadGrid(final List<String> url) {
        gridView.setAdapter(new ImageListAdapter(this, url, imageView));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
                //ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.zoom_in, R.anim.fade_out);
                intent.putExtra("url", url.get(position));
                startActivity(intent);//,activityOptions.toBundle());
            }
        });
    }

    public boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    public void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission Granted
            } else {
                //permission is not granted, Requesting again
                Toast.makeText(this, "Please click Allow to save Wallpapers in Device!", Toast.LENGTH_SHORT).show();
                //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
            }
        }
    }

    public List<String> convertURLsToHD(List<String> yourStringArray) {
        for (int i = 0; i < yourStringArray.size(); i++) {
            yourStringArray.set(i, yourStringArray.get(i).replaceAll("m.jpg", "h.jpg"));
            yourStringArray.set(i, yourStringArray.get(i).replaceAll("m.png", "h.png"));
            yourStringArray.set(i, yourStringArray.get(i).replaceAll("m.jpeg", "h.jepg"));
        }
        return yourStringArray;
    }

    private void checkForUpdates() {
        if (!CheckConnection.isOnline(this)) {
            Alerter.create(MainActivity.this).setText("No Internet Connection!!!").setTitle("Oops!").setIcon(R.drawable.error).setDuration(7000).enableSwipeToDismiss().show();
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("CurrentVersion");
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                };

                List<String> yourStringArray = dataSnapshot.getValue(t);
                currentVersion = yourStringArray.get(0);
                if (Integer.parseInt(currentVersion) > 1) {
                    Toast.makeText(MainActivity.this, "Please Update the app ", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "App is updated", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}

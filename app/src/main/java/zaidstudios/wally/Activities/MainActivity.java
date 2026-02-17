package zaidstudios.wally.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zaidstudios.wally.Helper.CheckConnection;
import zaidstudios.wally.Helper.GridSpacingItemDecoration;
import zaidstudios.wally.Helper.WallpaperAdapter;
import zaidstudios.wally.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    RecyclerView recyclerView;
    ImageView imageView;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int IMAGE_ACTIVITY_REQUEST_CODE = 102;
    int currentVersion = 2;

    private DatabaseReference mDatabaseRef;
    private ProgressBar progressBar;
    boolean loadHDThumb;
    boolean doubleBackToExitPressedOnce;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        doubleBackToExitPressedOnce = false;

        loadHDThumb = getSharedPreferences("isHDThumb", MODE_PRIVATE).getBoolean("HDThumb", false);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progress);
        imageView = findViewById(R.id.imageView);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        recyclerView.setVisibility(View.INVISIBLE);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, 10, true));

        if (!isStoragePermissionGranted()) {
            requestStoragePermission();
        }

        checkForUpdates();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        loadCategories();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Refresh the downloaded wallpapers list
            if (getTitle().equals("Downloads")) {
                loadDownloadedWallpapers();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

    public void loadDownloadedWallpapers() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Wallpy";
        File directory = new File(path);
        List<String> downloadedWallpapers = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file != null && file.isFile()) {
                        downloadedWallpapers.add(file.getAbsolutePath());
                    }
                }
            }
        }

        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        loadRecyclerView(downloadedWallpapers);
        if (downloadedWallpapers.isEmpty()) {
            Toast.makeText(this, "No downloaded wallpapers found.", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadUrlsFromFirebase(String category) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        if (!CheckConnection.isOnline(this)) {
            Alerter.create(MainActivity.this).setText("No Internet Connection!!!").setTitle("Oops!").setIcon(R.drawable.error).setDuration(7000).enableSwipeToDismiss().show();
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(category);
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() { };
                List<String> yourStringArray = dataSnapshot.getValue(t);

                if (yourStringArray != null) {
                    if (loadHDThumb) {
                        yourStringArray = convertURLsToHD(yourStringArray);
                    }
                    Collections.reverse(yourStringArray);
                    recyclerView.setVisibility(View.VISIBLE);
                    loadRecyclerView(yourStringArray);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference();
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Menu menu = navigationView.getMenu();
                menu.clear(); // Clear existing menu items

                // Add static menu items first
                menu.add(R.id.group_static, R.id.nav_all, Menu.NONE, "New").setIcon(R.drawable.ic_sync_black_24dp).setCheckable(true);
                menu.add(R.id.group_static, R.id.downlaods, Menu.NONE, "Downloads").setIcon(R.drawable.ic_info_black_24dp).setCheckable(true);

                // Dynamically add categories from Firebase
                List<String> categoryList = new ArrayList<>();
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String categoryName = categorySnapshot.getKey();
                    if (categoryName != null && !categoryName.equals("CurrentVersion") && !categoryName.equals("currentVersionCode")) {
                        categoryList.add(categoryName);
                    }
                }
                Collections.sort(categoryList);
                for(String categoryName : categoryList){
                    menu.add(R.id.group_dynamic, Menu.NONE, Menu.NONE, categoryName).setIcon(R.drawable.ic_notifications_black_24dp).setCheckable(true);
                }
                // Set the first item as selected
                if (menu.size() > 0) {
                    MenuItem firstItem = menu.getItem(0);
                    firstItem.setChecked(true);
                    onNavigationItemSelected(firstItem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load categories.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        String title = item.getTitle().toString();

        if (id == R.id.nav_all) {
            loadUrlsFromFirebase("All");
            setTitle("New");
        } else if (id == R.id.downlaods) {
            loadDownloadedWallpapers();
            setTitle("Downloads");
        } else {
            loadUrlsFromFirebase(title);
            setTitle(title);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadRecyclerView(final List<String> url) {
        recyclerView.setAdapter(new WallpaperAdapter(this, url));
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
        ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. You cannot view downloaded wallpapers.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public List<String> convertURLsToHD(List<String> yourStringArray) {
        for (int i = 0; i < yourStringArray.size(); i++) {
            yourStringArray.set(i, yourStringArray.get(i).replaceAll("m.jpg", ".jpg"));
            yourStringArray.set(i, yourStringArray.get(i).replaceAll("m.png", ".png"));
            yourStringArray.set(i, yourStringArray.get(i).replaceAll("m.jpeg", ".jpeg"));
        }
        return yourStringArray;
    }

    private void checkForUpdates() {
        if (!CheckConnection.isOnline(this)) {
            Alerter.create(MainActivity.this).setText("No Internet Connection!!!").setTitle("Oops!").setIcon(R.drawable.error).setDuration(7000).enableSwipeToDismiss().show();
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("CurrentVersion");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() { };
                List<String> yourStringArray = dataSnapshot.getValue(t);
                if (yourStringArray != null && !yourStringArray.isEmpty()) {
                    String actualVersion = yourStringArray.get(0);
                    if (Integer.parseInt(actualVersion) > currentVersion) {
                        Toast.makeText(MainActivity.this, "Please Update the app ", Toast.LENGTH_SHORT).show();
                    } else {
                        // Toast.makeText(MainActivity.this, "App is updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

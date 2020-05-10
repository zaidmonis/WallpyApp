package zaidstudios.wally.Activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import zaidstudios.wally.R;

public class SettingsActivity extends AppCompatActivity {

    CheckBox thumbCheckBox;

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        thumbCheckBox = findViewById(R.id.thumb_checkBox);
        thumbCheckBox.setChecked(getSharedPreferences("isHDThumb", MODE_PRIVATE).getBoolean("HDThumb", false));

        thumbCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (thumbCheckBox.isChecked()){
                    getSharedPreferences("isHDThumb", MODE_PRIVATE).edit().putBoolean("HDThumb", true).commit();
                }
                else{
                    getSharedPreferences("isHDThumb", MODE_PRIVATE).edit().putBoolean("HDThumb", false).commit();
                }
            }
        });




    }
}

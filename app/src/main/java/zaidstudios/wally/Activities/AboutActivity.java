package zaidstudios.wally.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import zaidstudios.wally.Helper.SampleHelper;
import zaidstudios.wally.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        SampleHelper.with(this).init().loadAbout();
    }
}

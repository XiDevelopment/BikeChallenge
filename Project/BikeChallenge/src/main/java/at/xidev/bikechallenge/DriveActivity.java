package at.xidev.bikechallenge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class DriveActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
    }

    // TEST ADI
    // TODO REMOVE
    public void buttonOnClick(View view) {
        Intent intent = new Intent(DriveActivity.this, SocialActivity.class);
        startActivity(intent);
    }
}

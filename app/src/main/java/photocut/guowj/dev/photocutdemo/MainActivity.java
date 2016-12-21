package photocut.guowj.dev.photocutdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnCorpPhoto, btnFramPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCorpPhoto = (Button) findViewById(R.id.btn_crop_photo);
        btnFramPhoto = (Button) findViewById(R.id.btn_photo_fram);

        btnCorpPhoto.setOnClickListener(this);
        btnFramPhoto.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == btnCorpPhoto) {
            Intent inte = new Intent(MainActivity.this, PhotoCropActivity.class);
            startActivity(inte);
        } else if (v == btnFramPhoto) {
            Intent inte = new Intent(MainActivity.this, PhotoFramActivity.class);
            startActivity(inte);
        }

    }
}

package ua.aengussong.www.bucketlist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ViewWishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wish);

        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            Toast toast = Toast.makeText(this,intent.getStringExtra(Intent.EXTRA_TEXT),Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}

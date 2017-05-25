package ua.aengussong.www.bucketlist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class AddWishActivity extends AppCompatActivity {

    private EditText add_title_edit;
    private EditText add_price_edit;
    private EditText add_category_edit;
    private EditText add_description_edit;
    private EditText add_target_date_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wish);

        add_title_edit = (EditText) findViewById(R.id.add_title_edit);
        add_price_edit = (EditText) findViewById(R.id.add_price_edit);
        add_category_edit = (EditText) findViewById(R.id.add_category_edit);
        add_description_edit = (EditText) findViewById(R.id.add_description_edit);
        add_target_date_edit = (EditText) findViewById(R.id.add_target_date_edit);
    }
}

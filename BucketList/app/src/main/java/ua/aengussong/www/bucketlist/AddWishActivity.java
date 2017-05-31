package ua.aengussong.www.bucketlist;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ua.aengussong.www.bucketlist.BucketListContracts.WishList;

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

    public void onClickAddWish(View view){
        String addTitle, addPrice, addCategory, addDescription, addTargetDate;
        if((addTitle = add_title_edit.getText().toString()).equals(""))
            return;
        if((addPrice = add_price_edit.getText().toString()).equals(""))
            return;
        if((addCategory = add_category_edit.getText().toString()).equals(""))
            return;
        if((addDescription = add_description_edit.getText().toString()).equals(""))
            return;
        if((addTargetDate = add_target_date_edit.getText().toString()).equals(""))
            return;


        ContentValues contentValues = new ContentValues();

        contentValues.put(WishList.COLUMN_TITLE, addTitle);
        //contentValues.put(WishList.COLUMN_PRICE, addPrice);
        //contentValues.put(WishList.COLUMN_CATEGORY, addCategory);
        contentValues.put(WishList.COLUMN_DESCRIPTION, addDescription);
        //contentValues.put(WishList.COLUMN_TARGET_DATE, addTargetDate);

        Uri uri = getContentResolver().insert(WishList.CONTENT_URI, contentValues);

        if(uri != null)
            Toast.makeText(this,uri.toString(),Toast.LENGTH_SHORT).show();

        finish();
    }
}

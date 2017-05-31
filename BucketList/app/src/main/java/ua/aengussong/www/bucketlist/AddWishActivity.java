package ua.aengussong.www.bucketlist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import ua.aengussong.www.bucketlist.BucketListContracts.WishList;

public class AddWishActivity extends AppCompatActivity {

    private EditText add_title_edit;
    private EditText add_price_edit;
    private EditText add_category_edit;
    private EditText add_description_edit;
    private EditText add_target_date_edit;

    private ConstraintLayout add_wish_constraint_layout;

    private ImageView imaginarium;

    private static int RESULT_LOAD_IMG  = 1;
    private String imgDecodableString;
    private Bitmap galleryImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wish);

        add_title_edit = (EditText) findViewById(R.id.add_title_edit);
        add_price_edit = (EditText) findViewById(R.id.add_price_edit);
        add_category_edit = (EditText) findViewById(R.id.add_category_edit);
        add_description_edit = (EditText) findViewById(R.id.add_description_edit);
        add_target_date_edit = (EditText) findViewById(R.id.add_target_date_edit);

        add_wish_constraint_layout = (ConstraintLayout) findViewById(R.id.add_wish_constraint_layout);

       imaginarium = (ImageView) findViewById(R.id.image_view_add_wish);
     /*   imaginarium.getSettings().setBuiltInZoomControls(true);
        imaginarium.getSettings().setSupportZoom(true);
        imaginarium.getSettings().setLoadWithOverviewMode(true);*/

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.add_wish);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_close_activity:
                finish();
                break;
        }
        return true;
    }


    public void loadImageFromGallery(View view){

        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        try{
            if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null){
                Uri selectedImage = data.getData();
                String[] filePathCoumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathCoumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathCoumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                galleryImage = BitmapFactory.decodeFile(imgDecodableString);

                Bitmap blurredImage = BlurBuilder.blur(this, galleryImage);

                setImageOnBackground(blurredImage, selectedImage);
            } else{
                Toast.makeText(this,"You Haven't Picked Image", Toast.LENGTH_SHORT).show();
            }
    /*    } catch(Exception e){
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }*/
    }

    private void setImageOnBackground(Bitmap bitmap, Uri er){
        Drawable drawableImage = new BitmapDrawable(getResources(),bitmap);
        //add_wish_constraint_layout.setBackground(drawableImage);
        imaginarium.setImageBitmap(bitmap);
//        imaginarium.loadUrl(er.toString());
    }

    public void onClickAddWish(View view){
        String addTitle, addPrice, addCategory, addDescription, addTargetDate;
        if((addTitle = add_title_edit.getText().toString()).equals(""))
            return;
        addPrice = add_price_edit.getText().toString();
        addCategory = add_category_edit.getText().toString();
        addDescription = add_description_edit.getText().toString();
        addTargetDate = add_target_date_edit.getText().toString();


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

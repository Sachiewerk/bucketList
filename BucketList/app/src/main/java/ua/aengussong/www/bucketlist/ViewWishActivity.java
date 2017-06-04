package ua.aengussong.www.bucketlist;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import ua.aengussong.www.bucketlist.database.BucketListContracts.*;
import ua.aengussong.www.bucketlist.utilities.DbBitmapUtility;
import ua.aengussong.www.bucketlist.utilities.DbQuery;

public class ViewWishActivity extends AppCompatActivity {

    ImageView viewImage;
    TextView viewTitle;
    TextView viewCategory;
    TextView viewDescription;
    TextView viewPrice;
    TextView viewTargetDate;

    LinearLayout viewLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wish);

        viewImage = (ImageView) findViewById(R.id.view_wish_image);
        viewTitle = (TextView)findViewById(R.id.view_wish_title);
        viewCategory = (TextView) findViewById(R.id.view_wish_category);
        viewDescription = (TextView) findViewById(R.id.view_wish_description);
        viewPrice = (TextView) findViewById(R.id.view_wish_price);
        viewTargetDate = (TextView) findViewById(R.id.view_wish_target_date);

        viewLinearLayout = (LinearLayout) findViewById(R.id.view_wish_linearLayout);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.view_wish_toolbar);
        setSupportActionBar(mActionBarToolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.your_wish);

        Intent intent = getIntent();
        String id="";
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            id = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        viewWish(id);

        viewMilestones(id);
    }

    private void viewWish(String id){
        Uri uri = WishList.CONTENT_URI;
        uri = uri.buildUpon().appendPath(id).build();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if(cursor.getCount() == 0)
            finish();
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndex(WishList.COLUMN_TITLE));
        String description = cursor.getString(cursor.getColumnIndex(WishList.COLUMN_DESCRIPTION));
        String target_date = cursor.getString(cursor.getColumnIndex(WishList.COLUMN_TARGET_DATE));
        int price = cursor.getInt(cursor.getColumnIndex(WishList.COLUMN_PRICE));
        int categoryId = cursor.getInt(cursor.getColumnIndex(WishList.COLUMN_CATEGORY));
        byte[] imageArray = cursor.getBlob(cursor.getColumnIndex(WishList.COLUMN_IMAGE));
        cursor.close();

        String category = DbQuery.getCategoryTitle(this, categoryId);

        viewImage.setImageBitmap(DbBitmapUtility.getImage(imageArray));
        viewTitle.setText(title);
        viewCategory.setText(category);
        viewDescription.setText(description);
        viewPrice.setText(price+"");
        viewTargetDate.setText(target_date);
    }

    private void viewMilestones(String id){
        Uri uri = Milestone.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null, Milestone.COLUMN_WISH + "=?", new String[]{id}, null);

        if(cursor.getCount() == 0)
            return;

        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            CheckBox chk = new CheckBox(this);

            chk.setTag(cursor.getInt(cursor.getColumnIndex(Milestone._ID)));

            String title = cursor.getString(cursor.getColumnIndex(Milestone.COLUMN_TITLE));
            chk.setText(title);
            int checked = cursor.getInt(cursor.getColumnIndex(Milestone.COLUMN_ACHIEVED));
            boolean isChecked = checked == 1;
            chk.setChecked(isChecked);

            chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int milestoneId = (int) buttonView.getTag();
                    updateMilestone(milestoneId, isChecked);
                }
            });

            viewLinearLayout.addView(chk);

            cursor.moveToNext();
        }


    }

    private void updateMilestone(int id, boolean isChecked){
        Uri uri = Milestone.CONTENT_URI;
        uri = uri.buildUpon().appendPath(id+"").build();

        ContentValues cv = new ContentValues();

        int checkedInt = isChecked ? 1 : 0;
        cv.put(Milestone.COLUMN_ACHIEVED, checkedInt);

        int updated =  getContentResolver().update(uri, cv, null, null);

        if (updated != 0 )
            Toast.makeText(this, updated+"", Toast.LENGTH_SHORT).show();

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
}

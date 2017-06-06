package ua.aengussong.www.bucketlist;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jinatonic.confetti.CommonConfetti;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ua.aengussong.www.bucketlist.database.BucketListContracts;
import ua.aengussong.www.bucketlist.database.BucketListContracts.*;
import ua.aengussong.www.bucketlist.utilities.DbBitmapUtility;
import ua.aengussong.www.bucketlist.utilities.DbQuery;

import static ua.aengussong.www.bucketlist.utilities.DbQuery.updateMilestone;

public class ViewWishActivity extends AppCompatActivity {

    ImageView viewImage;
    TextView viewTitle;
    TextView viewCategory;
    TextView viewDescription;
    TextView viewPrice;
    TextView viewTargetDate;

    LinearLayout viewLinearLayout;

    ViewGroup container;

    Button achievedButton;

    String wishId;

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

        achievedButton = (Button) findViewById(R.id.view_wish_achieved_button);

        viewLinearLayout = (LinearLayout) findViewById(R.id.view_wish_linearLayout);
        //provide container for confetti
        container = (ViewGroup) findViewById(R.id.view_frame_layout);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.view_wish_toolbar);
        setSupportActionBar(mActionBarToolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.your_wish);

        Intent intent = getIntent();

        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            wishId = intent.getStringExtra(Intent.EXTRA_TEXT);
        } else {
            finish();
        }

        viewWish(wishId);

        populateEditMilestones(wishId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewImage.setImageDrawable(null);
        viewWish(wishId);
        viewLinearLayout.removeAllViewsInLayout();
        populateEditMilestones(wishId);
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
        String achieved_date = cursor.getString(cursor.getColumnIndex(WishList.COLUMN_ACHIEVED_DATE));
        int price = cursor.getInt(cursor.getColumnIndex(WishList.COLUMN_PRICE));
        int categoryId = cursor.getInt(cursor.getColumnIndex(WishList.COLUMN_CATEGORY));
        byte[] imageArray = cursor.getBlob(cursor.getColumnIndex(WishList.COLUMN_IMAGE));
        cursor.close();

        String category = DbQuery.getCategoryTitle(this, categoryId);
        if(imageArray != null)
            viewImage.setImageBitmap(DbBitmapUtility.getImage(imageArray));
        viewTitle.setText(title);
        viewCategory.setText(category);
        viewDescription.setText(description);
        viewPrice.setText(price+"");
        viewTargetDate.setText(target_date);

        if(achieved_date != null)
            achievedButton.setVisibility(View.INVISIBLE);
    }


    public void achievedClicked(View view){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        achievedButton.setVisibility(View.INVISIBLE);

                        ContentValues achievedContentValue = new ContentValues();
                        achievedContentValue.put(WishList.COLUMN_ACHIEVED_DATE, getDateTime());

                        Uri uri = BucketListContracts.WishList.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(wishId).build();

                        getContentResolver().update(uri, achievedContentValue, null, null);

                        getContentResolver().delete(Milestone.CONTENT_URI, Milestone.COLUMN_WISH + "=?", new String[]{wishId});

                        MediaPlayer.create(ViewWishActivity.this, R.raw.fanfare).start();
                        CommonConfetti.rainingConfetti(container, new int[]{Color.BLUE, Color.GREEN, Color.YELLOW}).stream(10_000);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void populateEditMilestones(String id){
        Uri baseMilestoneUri = BucketListContracts.Milestone.CONTENT_URI;
        Cursor milestoneCursor = getContentResolver().query(baseMilestoneUri, null,
                BucketListContracts.Milestone.COLUMN_WISH + "=?", new String[]{id}, null);

        if(milestoneCursor.getCount() == 0)
            return;

        milestoneCursor.moveToFirst();
        while(!milestoneCursor.isAfterLast()){
            CheckBox chk = new CheckBox(this);

            chk.setTag(milestoneCursor.getInt(milestoneCursor.getColumnIndex(BucketListContracts.Milestone._ID)));

            String title = milestoneCursor.getString(milestoneCursor.getColumnIndex(BucketListContracts.Milestone.COLUMN_TITLE));

            chk.setText(title);
            int checked = milestoneCursor.getInt(milestoneCursor.getColumnIndex(BucketListContracts.Milestone.COLUMN_ACHIEVED));
            boolean isChecked = checked == 1;
            chk.setChecked(isChecked);

            chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int milestoneId = (int) buttonView.getTag();
                    updateMilestone(ViewWishActivity.this, milestoneId, isChecked);
                }
            });

            viewLinearLayout.addView(chk);

            milestoneCursor.moveToNext();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.close_wish_menu_item:
                finish();
                break;

            case R.id.edit_wish_menu_item:
                Intent intent = new Intent(ViewWishActivity.this, WishHandlingActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, wishId);

                startActivity(intent);
        }
        return true;
    }
}

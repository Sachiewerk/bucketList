package ua.aengussong.www.bucketlist.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
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

import ua.aengussong.www.bucketlist.R;
import ua.aengussong.www.bucketlist.database.BucketListContracts;
import ua.aengussong.www.bucketlist.database.BucketListContracts.Milestone;
import ua.aengussong.www.bucketlist.database.BucketListContracts.WishList;
import ua.aengussong.www.bucketlist.utilities.DbBitmapUtility;
import ua.aengussong.www.bucketlist.utilities.DbQuery;

import static android.support.v4.app.NotificationCompat.DEFAULT_LIGHTS;
import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;
import static android.view.View.INVISIBLE;
import static ua.aengussong.www.bucketlist.utilities.DbQuery.updateMilestone;

public class ViewWishActivity extends AppCompatActivity {

    ImageView viewImage;
    TextView viewTitle;
    TextView viewCategory;
    TextView viewDescription;
    TextView viewPrice;
    TextView viewTargetDate;
    TextView viewMilestonesTitle;

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
        viewMilestonesTitle = (TextView) findViewById(R.id.view_wish_milestones);

        achievedButton = (Button) findViewById(R.id.view_wish_achieved_button);

        viewLinearLayout = (LinearLayout) findViewById(R.id.view_wish_linearLayout);
        //provide container for confetti
        container = (ViewGroup) findViewById(R.id.view_frame_layout);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.view_wish_toolbar);
        setSupportActionBar(mActionBarToolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

        if(imageArray != null) {
            viewImage.requestLayout();
            viewImage.getLayoutParams().height = 400;

            viewImage.setImageBitmap(DbBitmapUtility.getImage(imageArray));
        } else{
            viewImage.requestLayout();
            viewImage.getLayoutParams().height=1;
            viewImage.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            viewImage.setImageDrawable(null);
        }
        String firstPart = getString(R.string.hint_title);
        SpannableStringBuilder ssbTitle = new SpannableStringBuilder(firstPart + " " +title);
        ssbTitle.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, firstPart.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewTitle.setText(ssbTitle);

        firstPart = getString(R.string.hint_category);
        SpannableStringBuilder ssbCategory = new SpannableStringBuilder(firstPart + " " + category);
        ssbCategory.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, firstPart.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewCategory.setText(ssbCategory);
        if(category.equals(""))
            viewCategory.setVisibility(INVISIBLE);

        firstPart = getString(R.string.hint_description);
        SpannableStringBuilder ssbDescription = new SpannableStringBuilder(firstPart + " " + description);
        ssbDescription.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, firstPart.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewDescription.setText(ssbDescription);
        if(description == null || description.equals(""))
            viewDescription.setVisibility(INVISIBLE);

        firstPart = getString(R.string.hint_price);
        SpannableStringBuilder ssbPrice = new SpannableStringBuilder(firstPart + " " + price);
        ssbPrice.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, firstPart.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewPrice.setText(ssbPrice);

        firstPart = getString(R.string.hint_target_date);
        SpannableStringBuilder ssbTargetDate = new SpannableStringBuilder(firstPart + " " + target_date);
        ssbTargetDate.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, firstPart.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewTargetDate.setText(ssbTargetDate);
        if(target_date == null || target_date.equals(""))
            viewTargetDate.setVisibility(INVISIBLE);

        if(achieved_date != null)
            achievedButton.setVisibility(INVISIBLE);
    }

    public void achievedClicked(View view){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        achievedButton.setVisibility(INVISIBLE);

                        ContentValues achievedContentValue = new ContentValues();
                        achievedContentValue.put(WishList.COLUMN_ACHIEVED_DATE, getDateTime());

                        Uri uri = BucketListContracts.WishList.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(wishId).build();

                        getContentResolver().update(uri, achievedContentValue, null, null);

                        getContentResolver().delete(Milestone.CONTENT_URI, Milestone.COLUMN_WISH + "=?", new String[]{wishId});

                        CommonConfetti.rainingConfetti(container, new int[]{Color.BLUE, Color.GREEN, Color.YELLOW}).stream(5_000);

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ViewWishActivity.this);
                        boolean sound = sharedPreferences.getBoolean("achieved_sound", true);
                        if(sound) {
                            MediaPlayer.create(ViewWishActivity.this, R.raw.achieved).start();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View sureView = factory.inflate(R.layout.are_you_sure_image_layout, null);
        builder.setView(sureView);
        builder.setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
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

        if(milestoneCursor.getCount() == 0) {
            viewMilestonesTitle.setVisibility(View.INVISIBLE);
            return;
        }

        milestoneCursor.moveToFirst();
        while(!milestoneCursor.isAfterLast()){
            CheckBox chk = new CheckBox(this);

            chk.setTag(milestoneCursor.getInt(milestoneCursor.getColumnIndex(BucketListContracts.Milestone._ID)));

            String title = milestoneCursor.getString(milestoneCursor.getColumnIndex(BucketListContracts.Milestone.COLUMN_TITLE));

            chk.setText(title);
            chk.setTextSize(18);
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

    public void onCloseWishMenuButton(View view){
        finish();
    }

    public void onEditWishMenuButton(View view){
        Intent intent = new Intent(ViewWishActivity.this, WishHandlingActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, wishId);

        startActivity(intent);
    }
}

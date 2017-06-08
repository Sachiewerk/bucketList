package ua.aengussong.www.bucketlist;

import android.graphics.PorterDuff;
import android.support.v4.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ua.aengussong.www.bucketlist.database.BucketListContracts;
import ua.aengussong.www.bucketlist.database.BucketListContracts.WishList;
import ua.aengussong.www.bucketlist.utilities.BlurBuilder;
import ua.aengussong.www.bucketlist.utilities.DatePicker;
import ua.aengussong.www.bucketlist.utilities.DbBitmapUtility;
import ua.aengussong.www.bucketlist.RVAddMilestoneAdapter.*;
import ua.aengussong.www.bucketlist.utilities.DbQuery;

import static ua.aengussong.www.bucketlist.utilities.DbQuery.updateMilestone;


public class WishHandlingActivity extends AppCompatActivity {

    private EditText add_title_edit;
    private EditText add_price_edit;
    private EditText add_description_edit;
    private EditText add_target_date_edit;
    private EditText add_milestone_edit;

    private Spinner categorySpinner;

    private ImageView backgroundWishImage;

    private RecyclerView milestonesRecyclerView;

    private Button addButton;
    private Button updateButton;

    private ImageButton deleteImage;

    private TextView toolbarTitle;

    private static int RESULT_LOAD_IMG  = 1;
    private String imgDecodableString;
    private Bitmap galleryImage = null;

    private RVAddMilestoneAdapter adapter;

    private ArrayList<String> milestonesArrayList = new ArrayList<>();

    private long milestoneWishId;

    private String editedWishId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_handling);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = (TextView) findViewById(R.id.wish_handling_toolbar_title);

        add_title_edit = (EditText) findViewById(R.id.add_title_edit);
        add_price_edit = (EditText) findViewById(R.id.add_price_edit);
        add_description_edit = (EditText) findViewById(R.id.add_description_edit);
        add_target_date_edit = (EditText) findViewById(R.id.add_target_date_edit);
        add_milestone_edit = (EditText) findViewById(R.id.add_milestones_edit);

        backgroundWishImage = (ImageView) findViewById(R.id.image_view_add_wish);
        backgroundWishImage.setColorFilter(R.color.colorPrimaryDark, PorterDuff.Mode.DARKEN);

        deleteImage = (ImageButton) findViewById(R.id.delete_image_button);

        categorySpinner = (Spinner) findViewById(R.id.category_spinner);

        milestonesRecyclerView = (RecyclerView) findViewById(R.id.rv_add_milestones);

        addButton = (Button) findViewById(R.id.add_wish_button);
        updateButton = (Button) findViewById(R.id.update_wish_button);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        milestonesRecyclerView.setLayoutManager(llm);
        milestonesRecyclerView.hasFixedSize();
        adapter = new RVAddMilestoneAdapter(this, milestonesArrayList);



        milestonesRecyclerView.setAdapter(adapter);


        populateCategorySpinner();

        Intent intent = getIntent();

        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            editedWishId = intent.getStringExtra(Intent.EXTRA_TEXT);
            if(editedWishId != null)
                goEditMode(editedWishId);
        } else {
            add_milestone_edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId,
                                              KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                            || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        milestonesArrayList.add(add_milestone_edit.getText().toString());
                        adapter.notifyDataSetChanged();
                        add_milestone_edit.setText("");
                        return true;
                    }
                    return false;
                }
            });

            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int id = (int) viewHolder.itemView.getTag();
                    milestonesArrayList.remove(id);

                    adapter.notifyDataSetChanged();
                }
            }).attachToRecyclerView(milestonesRecyclerView);
        }
    }

    public String getMilestoneId(String title, String wishId) {
        Cursor cursor = getContentResolver().query(BucketListContracts.Milestone.CONTENT_URI, null, "title=? and wish=?", new String[]{title, wishId}, null);
        String id;
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            id = cursor.getInt(cursor.getColumnIndex(BucketListContracts.Milestone._ID)) + "";
            return id;
        }
        return null;
    }

    private void populateMilestone(){
        Uri baseMilestoneUri = BucketListContracts.Milestone.CONTENT_URI;
        Cursor milestoneCursor = getContentResolver().query(baseMilestoneUri, null,
                BucketListContracts.Milestone.COLUMN_WISH + "=?", new String[]{editedWishId}, null);

        if(milestoneCursor.getCount() == 0)
            return;

        milestoneCursor.moveToFirst();
        while(!milestoneCursor.isAfterLast()){

            String title = milestoneCursor.getString(milestoneCursor.getColumnIndex(BucketListContracts.Milestone.COLUMN_TITLE));
            milestonesArrayList.add(title);

            milestoneCursor.moveToNext();
        }

    }

    private void goEditMode(String wishId){

        toolbarTitle.setText(getString(R.string.edit_wish));

        addButton.setVisibility(View.INVISIBLE);
        updateButton.setVisibility(View.VISIBLE);

        populateMilestone();
        adapter.notifyDataSetChanged();

        add_milestone_edit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    ContentValues cv = new ContentValues();
                    cv.put(BucketListContracts.Milestone.COLUMN_TITLE, add_milestone_edit.getText().toString());
                    cv.put(BucketListContracts.Milestone.COLUMN_WISH, editedWishId);
                    cv.put(BucketListContracts.Milestone.COLUMN_ACHIEVED, 0);

                    getContentResolver().insert(BucketListContracts.Milestone.CONTENT_URI,cv);
                    milestonesArrayList.add(add_milestone_edit.getText().toString());
                    adapter.notifyDataSetChanged();
                    add_milestone_edit.setText("");
                    return true;
                }
                return false;
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int id = (int) viewHolder.itemView.getTag();
                String milestoneId = getMilestoneId(milestonesArrayList.get(id), editedWishId);

                if(milestoneId != null){
                    Uri uri = BucketListContracts.Milestone.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(milestoneId).build();

                    getContentResolver().delete(uri, null, null);
                }

                milestonesArrayList.remove(id);

                adapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(milestonesRecyclerView);



        Uri uri = WishList.CONTENT_URI;
        uri = uri.buildUpon().appendPath(wishId).build();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if(cursor.getCount() == 0){ Toast.makeText(this, "fucker", Toast.LENGTH_LONG).show();
            finish();}
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

        add_title_edit.setText(title);
        add_description_edit.setText(description);
        add_target_date_edit.setText(target_date);
        add_price_edit.setText(price+"");

        categorySpinner.setSelection(getSpinnerItemIndex(categorySpinner, category));

        if(imageArray != null) {
            galleryImage = DbBitmapUtility.getImage(imageArray);
            Bitmap blurredImage = BlurBuilder.blur(this, galleryImage);
            backgroundWishImage.setImageBitmap(blurredImage);


            deleteImage.setVisibility(View.VISIBLE);
        }

        if(achieved_date != null){
            add_milestone_edit.setVisibility(View.INVISIBLE);
            add_target_date_edit.setVisibility(View.INVISIBLE);
        }
    }

    private int getSpinnerItemIndex(Spinner spinner, String itemTitle) {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(itemTitle)){
                index = i;
                break;
            }
        }
        return index;
    }

    public void loadImageFromGallery(View view){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null){
                deleteImage.setVisibility(View.VISIBLE);

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                galleryImage = BitmapFactory.decodeFile(imgDecodableString);

                Bitmap blurredImage = BlurBuilder.blur(this, galleryImage);

                backgroundWishImage.setImageBitmap(blurredImage);
            } else{
                Toast.makeText(this,"You Haven't Picked Image", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e){
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteSelectedImage(View view){
        backgroundWishImage.setImageDrawable(null);
        galleryImage = null;

        deleteImage.setVisibility(View.INVISIBLE);
    }

    public void showDatePickerDialog(View view){
        DialogFragment dateDialog = new DatePicker();
        dateDialog.show(getSupportFragmentManager(), "datePicker");
    }

    public void populateCategorySpinner(){
        String[] adapterCols=new String[]{BucketListContracts.Category.COLUMN_TITLE};
        int[] adapterRowViews=new int[]{android.R.id.text1};
        Cursor contentForSpinner = getContentResolver().query(BucketListContracts.Category.CONTENT_URI, null, null, null, null);
        SimpleCursorAdapter sca=new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, contentForSpinner, adapterCols, adapterRowViews,0);
        sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(sca);
    }

    public void onClickUpdateWish(View view){
        Uri uri = BucketListContracts.WishList.CONTENT_URI;
        uri = uri.buildUpon().appendPath(editedWishId).build();
        ContentValues cv;
        if((cv = setContentValues()) == null)
            return;
        int updateWish = getContentResolver().update(uri, cv , null, null);

        Toast.makeText(this, updateWish+"", Toast.LENGTH_SHORT);

        finish();
    }

    public void onClickAddWish(View view){
        ContentValues cv;
        if ((cv = setContentValues()) == null)
            return;
        Uri uri = getContentResolver().insert(WishList.CONTENT_URI, cv);

        if(uri != null){
            Toast.makeText(this,uri.toString(),Toast.LENGTH_SHORT).show();
            milestoneWishId = Long.parseLong(uri.getLastPathSegment());

            insertMilestones();
        }

        finish();
    }

    private ContentValues setContentValues(){
        String addTitle, addPrice, addCategory, addDescription, addTargetDate;
        if((addTitle = add_title_edit.getText().toString()).equals("")) {
            Toast.makeText(this, "Add Title", Toast.LENGTH_SHORT).show();
            return null;
        }

        addPrice = add_price_edit.getText().toString();
        //spinner returns cursor as selected item,so we get id from cursor to put into category column,
        //as it is foreign key
        Cursor categoryCursor = (Cursor)categorySpinner.getSelectedItem();
        addCategory = categoryCursor.getString(categoryCursor.getColumnIndex(BucketListContracts.Category._ID));
        addDescription = add_description_edit.getText().toString();
        addTargetDate = add_target_date_edit.getText().toString();


        ContentValues contentValues = new ContentValues();

        contentValues.put(WishList.COLUMN_TITLE, addTitle);
        if(!addPrice.equals(""))
            contentValues.put(WishList.COLUMN_PRICE, Integer.valueOf(addPrice));
        if(!addCategory.equals(""))
            contentValues.put(WishList.COLUMN_CATEGORY, Integer.valueOf(addCategory));
        if(!addDescription.equals(""))
            contentValues.put(WishList.COLUMN_DESCRIPTION, addDescription);
        //on DatePicker.class in onDataSet method String date value was put in add_target_date_edit tag
        if(!addTargetDate.equals(""))
            contentValues.put(WishList.COLUMN_TARGET_DATE, addTargetDate);

        byte[] bytes = null;
        if(galleryImage != null){
            //with large images CursorIndexOutOfBoundException was thrown, decided to scale images
            //to solve problem
            int nh = (int) ( galleryImage.getHeight() * (512.0 / galleryImage.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(galleryImage, 512, nh, true);

            bytes = DbBitmapUtility.getBytes(scaled);
        }
        contentValues.put(WishList.COLUMN_IMAGE, bytes);

        return  contentValues;
    }

    private void insertMilestones() {
        int count = adapter.getItemCount();
        MilestoneViewHolder holder;
        ContentValues cv;
        if (count == 0)
            return;
        else {
            for(int i=0;i<count;i++) {
                holder = (MilestoneViewHolder) milestonesRecyclerView.findViewHolderForAdapterPosition(i);
                String title = holder.newMilestone.getText().toString();
                boolean achievedBool = holder.newMilestone.isChecked();
                int achieved = achievedBool ? 1 : 0;

                cv = new ContentValues();
                cv.put(BucketListContracts.Milestone.COLUMN_TITLE,title);
                cv.put(BucketListContracts.Milestone.COLUMN_ACHIEVED,achieved);
                cv.put(BucketListContracts.Milestone.COLUMN_WISH, milestoneWishId);
                Uri uri = getContentResolver().insert(BucketListContracts.Milestone.CONTENT_URI, cv);

                if(uri != null) {
                    Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onCloseMenuButton(View view){
        finish();
    }

}

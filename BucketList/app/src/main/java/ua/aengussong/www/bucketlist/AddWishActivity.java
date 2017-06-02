package ua.aengussong.www.bucketlist;

import android.content.Context;
import android.provider.Settings;
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
import android.widget.EditText;
import android.widget.ImageView;
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

public class AddWishActivity extends AppCompatActivity {

    private EditText add_title_edit;
    private EditText add_price_edit;
    private EditText add_description_edit;
    private EditText add_target_date_edit;
    private EditText add_milestone_edit;

    private Spinner categorySpinner;

    private ImageView backgroundWishImage;

    private RecyclerView milestonesRecyclerView;

    private static int RESULT_LOAD_IMG  = 1;
    private String imgDecodableString;
    private Bitmap galleryImage = null;

    private RVAddMilestoneAdapter adapter;

    private ArrayList<String> milestonesArrayList = new ArrayList<>();

    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wish);

        add_title_edit = (EditText) findViewById(R.id.add_title_edit);
        add_price_edit = (EditText) findViewById(R.id.add_price_edit);
        add_description_edit = (EditText) findViewById(R.id.add_description_edit);
        add_target_date_edit = (EditText) findViewById(R.id.add_target_date_edit);
        add_milestone_edit = (EditText) findViewById(R.id.add_milestones_edit);


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

        backgroundWishImage = (ImageView) findViewById(R.id.image_view_add_wish);

        categorySpinner = (Spinner) findViewById(R.id.category_spinner);

        milestonesRecyclerView = (RecyclerView) findViewById(R.id.rv_add_milestones);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.add_wish);


        LinearLayoutManager llm = new LinearLayoutManager(this);
        milestonesRecyclerView.setLayoutManager(llm);
        milestonesRecyclerView.hasFixedSize();
        adapter = new RVAddMilestoneAdapter(this,milestonesArrayList);

        milestonesRecyclerView.setAdapter(adapter);
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

        populateCategorySpinner();
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

        try{
            if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null){
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

    public void onClickAddWish(View view){
        Toast.makeText(this,(String)add_target_date_edit.getTag(),Toast.LENGTH_SHORT).show();
        String addTitle, addPrice, addCategory, addDescription, addTargetDate;
        if((addTitle = add_title_edit.getText().toString()).equals("")) {
            Toast.makeText(this, "Add Title", Toast.LENGTH_SHORT).show();
            return;
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
            contentValues.put(WishList.COLUMN_TARGET_DATE, (String)add_target_date_edit.getTag());

        byte[] bytes;
        if(galleryImage != null){
            //with large images CursorIndexOutOfBoundException was thrown, decided to scale images
            //to solve problem
            int nh = (int) ( galleryImage.getHeight() * (512.0 / galleryImage.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(galleryImage, 512, nh, true);

            bytes = DbBitmapUtility.getBytes(scaled);
            contentValues.put(WishList.COLUMN_IMAGE, bytes);
        }

        Uri uri = getContentResolver().insert(WishList.CONTENT_URI, contentValues);

        if(uri != null){
            Toast.makeText(this,uri.toString(),Toast.LENGTH_SHORT).show();
            id = Long.parseLong(uri.getLastPathSegment());

            insertMilestones();
        }

        finish();
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
                boolean achieved = holder.newMilestone.isChecked();

                cv = new ContentValues();
                cv.put(BucketListContracts.Milestone.COLUMN_TITLE,title);
                cv.put(BucketListContracts.Milestone.COLUMN_ACHIEVED,achieved);
                cv.put(BucketListContracts.Milestone.COLUMN_WISH, id);
                Uri uri = getContentResolver().insert(BucketListContracts.Milestone.CONTENT_URI, cv);

                if(uri != null) {
                    Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}

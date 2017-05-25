package ua.aengussong.www.bucketlist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RVMainAdapter.WishClickListener {

    private int number = 20;

    private RVMainAdapter adapter;

    RecyclerView recyclerView;

    private SQLiteDatabase db;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab_add_wish);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddWishActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.rv_wishes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.hasFixedSize();

        BucketListDBHelper dbHelper = new BucketListDBHelper(this);
        db = dbHelper.getWritableDatabase();
        insertFakeData(db);
        Cursor cursor = getAllWishes();

        adapter = new RVMainAdapter(cursor, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onWishClicked(int clickedPosition) {
        Intent intent = new Intent(MainActivity.this, ViewWishActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(clickedPosition));
        startActivity(intent);
    }

    private Cursor getAllWishes(){
        return db.query(BucketListContracts.WishList.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public static void insertFakeData(SQLiteDatabase db){
        if(db == null){
            return;
        }
        //create a list of fake guests
        List<ContentValues> list = new ArrayList<ContentValues>();

        ContentValues cv = new ContentValues();
        cv.put(BucketListContracts.WishList.COLUMN_TITLE, "Skydive");
        cv.put(BucketListContracts.WishList.COLUMN_PRICE, 2000);
        cv.put(BucketListContracts.WishList.COLUMN_DESCRIPTION,"sdfasdfasdfasdf");
        list.add(cv);

        cv = new ContentValues();
        cv.put(BucketListContracts.WishList.COLUMN_TITLE, "Skydive");
        cv.put(BucketListContracts.WishList.COLUMN_PRICE, 4000);
        cv.put(BucketListContracts.WishList.COLUMN_DESCRIPTION,"qqqqqqqqqqqq");
        list.add(cv);

        cv = new ContentValues();
        cv.put(BucketListContracts.WishList.COLUMN_TITLE, "Skydive");
        cv.put(BucketListContracts.WishList.COLUMN_PRICE, 6000);
        cv.put(BucketListContracts.WishList.COLUMN_DESCRIPTION,"wwwwwwwwwwwww");
        list.add(cv);

        cv = new ContentValues();
        cv.put(BucketListContracts.WishList.COLUMN_TITLE, "Skydive");
        cv.put(BucketListContracts.WishList.COLUMN_PRICE, 8000);
        cv.put(BucketListContracts.WishList.COLUMN_DESCRIPTION,"eeeeeeeeeeeee");
        list.add(cv);

        cv = new ContentValues();
        cv.put(BucketListContracts.WishList.COLUMN_TITLE, "Skydive");
        cv.put(BucketListContracts.WishList.COLUMN_PRICE, 10000);
        cv.put(BucketListContracts.WishList.COLUMN_DESCRIPTION,"rrrrrrrrrrrrr");
        list.add(cv);

        //insert all guests in one transaction
        try
        {
            db.beginTransaction();
            //clear the table first
            db.delete (BucketListContracts.WishList.TABLE_NAME,null,null);
            //go through the list and add one by one
            for(ContentValues c:list){
                db.insert(BucketListContracts.WishList.TABLE_NAME, null, c);
            }
            db.setTransactionSuccessful();
        }
        catch (SQLException e) {
            //too bad :(
        }
        finally
        {
            db.endTransaction();
        }

    }
}

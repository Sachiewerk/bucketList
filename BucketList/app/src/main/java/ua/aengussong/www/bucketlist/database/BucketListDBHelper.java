package ua.aengussong.www.bucketlist.database;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ua.aengussong.www.bucketlist.R;
import ua.aengussong.www.bucketlist.database.BucketListContracts;
import ua.aengussong.www.bucketlist.database.BucketListContracts.*;

/**
 * Created by coolsmileman on 24.05.2017.
 */

public class BucketListDBHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "bucketlist.db";

    public static final int DATABASE_VERSION = 24;

    private Context context;

    BucketListDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_CATEGORY_TABLE = "CREATE TABLE "+
                Category.TABLE_NAME + " ("+
                Category._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                Category.COLUMN_TITLE + " TEXT NOT NULL "+
                ");";
        db.execSQL(CREATE_CATEGORY_TABLE);

        final String CREATE_WISHLIST_TABLE = "CREATE TABLE "+
                WishList.TABLE_NAME + " (" +
                WishList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                WishList.COLUMN_TITLE + " TEXT NOT NULL, "+
                WishList.COLUMN_IMAGE +  " BLOB," +
                WishList.COLUMN_PRICE + " INTEGER," +
                WishList.COLUMN_CATEGORY + " INTEGER," +
                WishList.COLUMN_DESCRIPTION + " TEXT, " +
                WishList.COLUMN_TARGET_DATE + " TEXT," +
                WishList.COLUMN_ACHIEVED_DATE + " TEXT," +
                "FOREIGN KEY (" + WishList.COLUMN_CATEGORY + ") REFERENCES " +
                    Category.TABLE_NAME + " (" + Category._ID+") ON DELETE SET NULL"+
                ");";
        db.execSQL(CREATE_WISHLIST_TABLE);

        final String CREATE_MILESTONE_TABLE = "CREATE TABLE " +
                Milestone.TABLE_NAME + " (" +
                Milestone._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Milestone.COLUMN_TITLE + " TEXT NOT NULL," +
                Milestone.COLUMN_ACHIEVED + " INTEGER NOT NULL, " +
                Milestone.COLUMN_WISH + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + Milestone.COLUMN_WISH + ") REFERENCES " + WishList.TABLE_NAME + " (" + WishList._ID + ") ON DELETE CASCADE" +
                ");";
        db.execSQL(CREATE_MILESTONE_TABLE);

        //initialize category table with some start values
        String[] categoryInitValues = context.getResources().getStringArray(R.array.category_init);
        for(String value : categoryInitValues){
            db.execSQL("INSERT INTO " + BucketListContracts.Category.TABLE_NAME +
                    "( " + BucketListContracts.Category.COLUMN_TITLE + " ) values ('"+value+"');");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Category.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WishList.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Milestone.TABLE_NAME);

        onCreate(db);
    }
    //to provide cascade deleting
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}

package ua.aengussong.www.bucketlist.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import ua.aengussong.www.bucketlist.database.BucketListContracts;

/**
 * Created by coolsmileman on 04.06.2017.
 */

public class DbQuery {
    public static String getCategoryTitle(Context context, int id){
        Uri uri = BucketListContracts.Category.CONTENT_URI;
        uri = uri.buildUpon().appendPath(id+"").build();
        Cursor categoryCursor = context.getContentResolver().query(uri, null, null, null, null);


        String categoryTitle = "";
        //prevents crush if query return no results
        if(categoryCursor.getCount()>0) {
            categoryCursor.moveToFirst();
            categoryTitle = categoryCursor.getString(categoryCursor.getColumnIndex(BucketListContracts.Category.COLUMN_TITLE));
        }
        categoryCursor.close();
        return categoryTitle;
    }

    public static void updateMilestone(Context context, int id, boolean isChecked){
        Uri uri = BucketListContracts.Milestone.CONTENT_URI;
        uri = uri.buildUpon().appendPath(id+"").build();

        ContentValues cv = new ContentValues();

        int checkedInt = isChecked ? 1 : 0;
        cv.put(BucketListContracts.Milestone.COLUMN_ACHIEVED, checkedInt);

        int updated =  context.getContentResolver().update(uri, cv, null, null);

        if (updated != 0 )
            Toast.makeText(context, updated+"", Toast.LENGTH_SHORT).show();

    }

}

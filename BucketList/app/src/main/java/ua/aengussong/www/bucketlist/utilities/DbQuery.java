package ua.aengussong.www.bucketlist.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

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

}

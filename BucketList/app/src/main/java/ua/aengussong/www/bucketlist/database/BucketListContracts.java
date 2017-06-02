package ua.aengussong.www.bucketlist.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by coolsmileman on 24.05.2017.
 */

public class BucketListContracts {

    public static final String AUTHORITY = "ua.aengussong.www.bucketlist";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_WISHLIST = "wishlist";
    public static final String PATH_MILESTONE = "milestone";
    public static final String PATH_CATEGORY = "category";

    private BucketListContracts(){}

    public static class WishList implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WISHLIST).build();

        public static final String TABLE_NAME = "wishlist";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TARGET_DATE = "target_date";
        public static final String COLUMN_ACHIEVED_DATE = "achieved_date";
    }

    public static class Milestone implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MILESTONE).build();

        public static final String TABLE_NAME = "milestone";

        public static final String COLUMN_WISH = "wish";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ACHIEVED = "achieved";
    }

    public static class Category implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String TABLE_NAME = " category ";

        public static final String COLUMN_TITLE = "title";
    }
}

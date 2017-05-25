package ua.aengussong.www.bucketlist;

import android.provider.BaseColumns;

/**
 * Created by coolsmileman on 24.05.2017.
 */

public class BucketListContracts {
    private BucketListContracts(){}

    public static class WishList implements BaseColumns{
        public static final String TABLE_NAME = "wishlist";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TARGET_DATE = "target date";
        public static final String COLUMN_ACHIEVED_DATE = "achieved date";
    }

    public static class Milestone implements BaseColumns{
        public static final String TABLE_NAME = "milestone";

        public static final String COLUMN_WISH = "wish";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ACHIEVED = "achieved";
    }

    public static class Category implements BaseColumns{
        public static final String TABLE_NAME = " category ";

        public static final String COLUMN_TITLE = "title";
    }
}

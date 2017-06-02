package ua.aengussong.www.bucketlist.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by coolsmileman on 29.05.2017.
 */

public class BucketListContentProvider extends ContentProvider {

    private BucketListDBHelper dbHelper;

    public static final int WISHES = 100;
    public static final int WISHES_WITH_ID = 101;

    public static final int MILESTONES = 200;
    public static final int MILESTONES_WITH_ID = 201;


    public static final int CATEGORIES = 300;
    public static final int CATEGORIES_WITH_ID = 301;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(BucketListContracts.AUTHORITY, BucketListContracts.PATH_WISHLIST, WISHES);
        uriMatcher.addURI(BucketListContracts.AUTHORITY, BucketListContracts.PATH_WISHLIST + "/#", WISHES_WITH_ID);

        uriMatcher.addURI(BucketListContracts.AUTHORITY, BucketListContracts.PATH_MILESTONE, MILESTONES);
        uriMatcher.addURI(BucketListContracts.AUTHORITY, BucketListContracts.PATH_MILESTONE + "/#", MILESTONES_WITH_ID);
        //uriMatcher.addURI(BucketListContracts.AUTHORITY, BucketListContracts.PATH_MILESTONE + "/wish/#", MILESTONES_WITH_WISH);

        uriMatcher.addURI(BucketListContracts.AUTHORITY, BucketListContracts.PATH_CATEGORY, CATEGORIES);
        uriMatcher.addURI(BucketListContracts.AUTHORITY, BucketListContracts.PATH_CATEGORY + "/#", CATEGORIES_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new BucketListDBHelper(context);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        Cursor retCursor;

        switch (match){
            case WISHES:
                retCursor = db.query(BucketListContracts.WishList.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case WISHES_WITH_ID:
                String wishId = uri.getPathSegments().get(1);

                String wishSelection = "_id=?";
                String[] wishSelectionArgs = new String[]{wishId};

                retCursor = db.query(BucketListContracts.WishList.TABLE_NAME,
                        projection,
                        wishSelection,
                        wishSelectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            case CATEGORIES:
                retCursor = db.query(BucketListContracts.Category.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CATEGORIES_WITH_ID:
                String categoryId = uri.getPathSegments().get(1);

                String categorySelection = "_id=?";
                String[] categorySelectionArgs = new String[]{categoryId};

                retCursor = db.query(BucketListContracts.Category.TABLE_NAME,
                        projection,
                        categorySelection,
                        categorySelectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            case MILESTONES:
                retCursor = db.query(BucketListContracts.Milestone.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case MILESTONES_WITH_ID:
                String milestoneId = uri.getPathSegments().get(1);

                String milestoneSelection = "_id=?";
                String[] milestoneSelectionArgs = new String[]{milestoneId};

                retCursor = db.query(BucketListContracts.Milestone.TABLE_NAME,
                        projection,
                        milestoneSelection,
                        milestoneSelectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            default: throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match){
            case WISHES:
                long wishes_id = db.insert(BucketListContracts.WishList.TABLE_NAME, null, values);
                if(wishes_id > 0){
                    returnUri = ContentUris.withAppendedId(BucketListContracts.WishList.CONTENT_URI, wishes_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case MILESTONES:
                long milestones_id = db.insert(BucketListContracts.Milestone.TABLE_NAME, null, values);
                if(milestones_id > 0){
                    returnUri = ContentUris.withAppendedId(BucketListContracts.Milestone.CONTENT_URI, milestones_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case CATEGORIES:
                long categories_id = db.insert(BucketListContracts.Category.TABLE_NAME, null, values);
                if(categories_id > 0){
                    returnUri = ContentUris.withAppendedId(BucketListContracts.Category.CONTENT_URI, categories_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default: throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted tasks
        int deleted; // starts as 0

        // Write the code to delete a single row of data
        // [Hint] Use selections to delete an item by its row ID
        switch (match) {
            // Handle the single item case, recognized by the ID included in the URI path
            case WISHES_WITH_ID:
                // Get the task ID from the URI path
                String wishId = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                deleted = db.delete(BucketListContracts.WishList.TABLE_NAME, "_id=?", new String[]{wishId});
                break;

            case CATEGORIES_WITH_ID:
                // Get the task ID from the URI path
                String categoryId = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                deleted = db.delete(BucketListContracts.Category.TABLE_NAME, "_id=?", new String[]{categoryId});
                break;

            case MILESTONES_WITH_ID:
                // Get the task ID from the URI path
                String milestoneId = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                deleted = db.delete(BucketListContracts.Milestone.TABLE_NAME, "_id=?", new String[]{milestoneId});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (deleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of tasks deleted
        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        //Keep track of if an update occurs
        int updated;

        // match code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case WISHES_WITH_ID:
                //update a single task by getting the id
                String wishId = uri.getPathSegments().get(1);
                //using selections
                updated = dbHelper.getWritableDatabase().update(BucketListContracts.WishList.TABLE_NAME, values, "_id=?", new String[]{wishId});
                break;

            case CATEGORIES_WITH_ID:
                //update a single task by getting the id
                String categoryId = uri.getPathSegments().get(1);
                //using selections
                updated = dbHelper.getWritableDatabase().update(BucketListContracts.Category.TABLE_NAME, values, "_id=?", new String[]{categoryId});
                break;

            case MILESTONES_WITH_ID:
                //update a single task by getting the id
                String milestoneId = uri.getPathSegments().get(1);
                //using selections
                updated = dbHelper.getWritableDatabase().update(BucketListContracts.Milestone.TABLE_NAME, values, "_id=?", new String[]{milestoneId});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updated != 0) {
            //set notifications if a task was updated
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return number of tasks updated
        return updated;
    }
}

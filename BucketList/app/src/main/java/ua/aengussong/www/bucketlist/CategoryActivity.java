package ua.aengussong.www.bucketlist;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import ua.aengussong.www.bucketlist.database.BucketListContracts;
import ua.aengussong.www.bucketlist.utilities.DbBitmapUtility;

public class CategoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = CategoryActivity.class.getSimpleName();
    private static final int CATEGORY_LOADER_ID = 1;


    private RVCategoryAdapter adapter;

    RecyclerView recyclerView;

    private FloatingActionButton fab;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.hint_category);

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryTitle = input.getText().toString();

                if(categoryTitle.equals("")) {
                    return;
                }
                ContentValues contentValues = new ContentValues();

                contentValues.put(BucketListContracts.Category.COLUMN_TITLE, categoryTitle);

                getContentResolver().insert(BucketListContracts.Category.CONTENT_URI, contentValues);
                getSupportLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, CategoryActivity.this);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab_add_category);
        fab.setImageResource(R.drawable.ic_fab_add_wish);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.show();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.rv_category);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.hasFixedSize();

        Toolbar categoryToolbar = (Toolbar) findViewById(R.id.category_toolbar);
        setSupportActionBar(categoryToolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        adapter = new RVCategoryAdapter(this);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete

                // COMPLETED (1) Construct the URI for the item to delete
                //[Hint] Use getTag (from the adapter code) to get the id of the swiped item
                // Retrieve the id of the task to delete
                int id = (int) viewHolder.itemView.getTag();

                // Build appropriate uri with String row id appended
                String stringId = Integer.toString(id);
                Uri uri = BucketListContracts.Category.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                // COMPLETED (2) Delete a single row of data using a ContentResolver
                /*ContentValues cv = new ContentValues();
                cv.put(BucketListContracts.WishList.COLUMN_CATEGORY, "");

                getContentResolver().update(BucketListContracts.WishList.CONTENT_URI, cv,
                        BucketListContracts.WishList.COLUMN_CATEGORY + "=?", new String[]{stringId});*/

                getContentResolver().delete(uri, null, null);


                // COMPLETED (3) Restart the loader to re-query for all tasks after a deletion
                getSupportLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, CategoryActivity.this);

            }
        }).attachToRecyclerView(recyclerView);

        getSupportLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);
    }

    public void wishSwiped(final RecyclerView.ViewHolder viewHolder){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // Here is where you'll implement swipe to delete

                        // COMPLETED (1) Construct the URI for the item to delete
                        //[Hint] Use getTag (from the adapter code) to get the id of the swiped item
                        // Retrieve the id of the task to delete
                        int id = (int) viewHolder.itemView.getTag();

                        // Build appropriate uri with String row id appended
                        String stringId = Integer.toString(id);
                        Uri uri = BucketListContracts.Category.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(stringId).build();

                        // COMPLETED (2) Delete a single row of data using a ContentResolver
                /*ContentValues cv = new ContentValues();
                cv.put(BucketListContracts.WishList.COLUMN_CATEGORY, "");

                getContentResolver().update(BucketListContracts.WishList.CONTENT_URI, cv,
                        BucketListContracts.WishList.COLUMN_CATEGORY + "=?", new String[]{stringId});*/

                        getContentResolver().delete(uri, null, null);


                        // COMPLETED (3) Restart the loader to re-query for all tasks after a deletion
                        getSupportLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, CategoryActivity.this);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        getSupportLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, CategoryActivity.this);
                        break;
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.are_you_sure_image_layout, null);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(CATEGORY_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mCategoryData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mCategoryData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mCategoryData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background; sort by priority
                // [Hint] use a try/catch block to catch any errors in loading data

                try {
                    return getContentResolver().query(BucketListContracts.Category.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mCategoryData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public void onCloseMenuButton(View view){
        finish();
    }
}

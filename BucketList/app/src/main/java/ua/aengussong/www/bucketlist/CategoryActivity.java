package ua.aengussong.www.bucketlist;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ua.aengussong.www.bucketlist.database.BucketListContracts;

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



        fab = (FloatingActionButton) findViewById(R.id.fab_add_category);
        fab.setImageResource(R.drawable.ic_fab_add_wish);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCategoryDialog();
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

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                categorySwiped(viewHolder);
            }
        }).attachToRecyclerView(recyclerView);

        getSupportLoaderManager().initLoader(CATEGORY_LOADER_ID, null, this);
    }

    private void showAddCategoryDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.hint_category);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(null);
        builder.setView(input);
        builder.setCancelable(false);


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

        builder.show();
    }

    public void categorySwiped(final RecyclerView.ViewHolder viewHolder){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        int id = (int) viewHolder.itemView.getTag();

                        String stringId = Integer.toString(id);
                        Uri uri = BucketListContracts.Category.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(stringId).build();

                        getContentResolver().delete(uri, null, null);

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

            Cursor mCategoryData = null;

            @Override
            protected void onStartLoading() {
                if (mCategoryData != null) {
                    deliverResult(mCategoryData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {

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

package ua.aengussong.www.bucketlist;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import ua.aengussong.www.bucketlist.database.BucketListContracts;
import ua.aengussong.www.bucketlist.database.BucketListContracts.WishList;

public class MainActivity extends AppCompatActivity implements RVMainAdapter.WishClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int WISH_LOADER_ID = 0;


    private RVMainAdapter adapter;

    RecyclerView recyclerView;

    private FloatingActionButton fab;

    String selection;

    Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab_add_wish);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WishHandlingActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.rv_wishes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.hasFixedSize();

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        //toolbar.setNavigationIcon(R.drawable.ic_toolbar);
        mainToolbar.setTitle("");

        setSupportActionBar(mainToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
/*
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.app_name);
*/

        adapter = new RVMainAdapter(this, this);
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
                Uri uri = WishList.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                // COMPLETED (2) Delete a single row of data using a ContentResolver
                getContentResolver().delete(uri, null, null);

                // COMPLETED (3) Restart the loader to re-query for all tasks after a deletion
                getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);

            }
        }).attachToRecyclerView(recyclerView);

        selection = WishList.COLUMN_ACHIEVED_DATE + " is null or " +  WishList.COLUMN_ACHIEVED_DATE + "=?";

        getSupportLoaderManager().initLoader(WISH_LOADER_ID, null, this);

        new DrawerBuilder().withActivity(this).build();

        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.achieved);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.category);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.app_name);

        //create the drawer and remember the `Drawer` result object
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mainToolbar)
                .addDrawerItems(
                        item3,
                        item1,
                        new DividerDrawerItem(),
                        item2
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        long identifier = drawerItem.getIdentifier();
                        switch ((int) identifier){
                            case 1: //achieved
                                moveDrawer();
                                selection = WishList.COLUMN_ACHIEVED_DATE + " <> ? ";
                                getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                                break;
                            case 2: //category
                                moveDrawer();
                                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                                startActivity(intent);
                                break;
                            case 3: //bucketList
                                moveDrawer();
                                selection = WishList.COLUMN_ACHIEVED_DATE + " is null or " +  WishList.COLUMN_ACHIEVED_DATE + "=?";
                                getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                        }
                        return true;
                    }
                })
                .build();
    }

    private void moveDrawer(){
        if (drawer.isDrawerOpen())
                drawer.closeDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, this);
    }

    @Override
    public void onWishClicked(View view) {
        Intent intent = new Intent(MainActivity.this, ViewWishActivity.class);
        int id = (int) view.getTag();
        intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(id));
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mWishData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mWishData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mWishData);
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
                    return getContentResolver().query(WishList.CONTENT_URI,
                            null,
                            selection,
                            new String[]{""},
                            WishList.COLUMN_TARGET_DATE);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mWishData = data;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch(item.getItemId()){
            case R.id.close_wish_menu_item:
                finish();
                break;

            case R.id.edit_wish_menu_item:
                Intent intent = new Intent(ViewWishActivity.this, WishHandlingActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, wishId);

                startActivity(intent);
        }*/
        return true;
    }


}

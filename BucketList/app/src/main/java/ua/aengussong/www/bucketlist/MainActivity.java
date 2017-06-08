package ua.aengussong.www.bucketlist;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jinatonic.confetti.CommonConfetti;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
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

    TextView toolbarTitle;

    int bucketCount;
    int achievedCount;
    int categoryCount;

    PrimaryDrawerItem achievedDrawerItem;
    SecondaryDrawerItem categoryDrawerItem;
    PrimaryDrawerItem bucketListDrawerItem;
    SecondaryDrawerItem backupDrawerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab_add_wish);
        fab.setImageResource(R.drawable.ic_fab_add_wish);
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
        toolbarTitle = (TextView) findViewById(R.id.main_toolbar_title);

        setSupportActionBar(mainToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
                wishSwiped(viewHolder);

            }
        }).attachToRecyclerView(recyclerView);

        selection = WishList.COLUMN_ACHIEVED_DATE + " is null or " +  WishList.COLUMN_ACHIEVED_DATE + "=?";

        getSupportLoaderManager().initLoader(WISH_LOADER_ID, null, this);

        //new DrawerBuilder().withActivity(this).build();

        AccountHeader headerDrawer = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_drawer_background)
                .build();

        achievedDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.achieved).withIcon(R.drawable.ic_achieved_drawer_item);
        categoryDrawerItem = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.category).withIcon(R.drawable.ic_category_drawer_image);
        bucketListDrawerItem = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.app_name).withIcon(R.drawable.ic_bucket_drawer_item);
        backupDrawerItem  = new SecondaryDrawerItem().withIdentifier(4).withName(R.string.backup);

        //create the drawer and remember the `Drawer` result object
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mainToolbar)
                .withAccountHeader(headerDrawer)
                .addDrawerItems(
                        bucketListDrawerItem,
                        achievedDrawerItem,
                        new DividerDrawerItem(),
                        categoryDrawerItem,
                        new DividerDrawerItem(),
                        backupDrawerItem,
                        new DividerDrawerItem()
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        long identifier = drawerItem.getIdentifier();
                        switch ((int) identifier){
                            case 1: //achieved
                                toolbarTitle.setText(getString(R.string.achieved));
                                moveDrawer();
                                selection = WishList.COLUMN_ACHIEVED_DATE + " <> ? ";
                                getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                                fab.setVisibility(View.INVISIBLE);
                                break;
                            case 2: //category
                                moveDrawer();
                                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                                startActivity(intent);
                                break;
                            case 3: //bucketList
                                toolbarTitle.setText(getString(R.string.app_name));
                                moveDrawer();
                                selection = WishList.COLUMN_ACHIEVED_DATE + " is null or " +  WishList.COLUMN_ACHIEVED_DATE + "=?";
                                getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                                fab.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }
                })
                .build();

        refreshBucketAchievedDrawerItem();
        refreshCategoryDrawerItem();

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
                        Uri uri = WishList.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(stringId).build();

                        // COMPLETED (2) Delete a single row of data using a ContentResolver
                        getContentResolver().delete(uri, null, null);

                        refreshBucketAchievedDrawerItem();

                        // COMPLETED (3) Restart the loader to re-query for all tasks after a deletion
                        getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.are_you_sure_image_layout, null);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    private void moveDrawer(){
        if (drawer.isDrawerOpen())
                drawer.closeDrawer();
    }

    private void refreshBucketAchievedDrawerItem(){
        bucketCount = getContentResolver().query(WishList.CONTENT_URI, null,
                WishList.COLUMN_ACHIEVED_DATE + " is null or " +  WishList.COLUMN_ACHIEVED_DATE + "=?",
                new String[]{""}, null).getCount();

        achievedCount = getContentResolver().query(WishList.CONTENT_URI, null,
                WishList.COLUMN_ACHIEVED_DATE + " <> ? ", new String[]{""}, null).getCount();

        bucketListDrawerItem.withBadge(bucketCount+"").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.primary_dark));
        achievedDrawerItem.withBadge(achievedCount+"").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.primary_dark));

        drawer.updateItem(bucketListDrawerItem);
        drawer.updateItem(achievedDrawerItem);
    }

    private void refreshCategoryDrawerItem(){
        categoryCount = getContentResolver().query(BucketListContracts.Category.CONTENT_URI, null, null, null, null).getCount();
        categoryDrawerItem.withBadge(categoryCount+"").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.primary_dark));
        drawer.updateItem(categoryDrawerItem);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, this);
        refreshBucketAchievedDrawerItem();
        refreshCategoryDrawerItem();
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

package ua.aengussong.www.bucketlist;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ua.aengussong.www.bucketlist.database.BucketListContracts;
import ua.aengussong.www.bucketlist.database.BucketListContracts.WishList;
import ua.aengussong.www.bucketlist.database.BucketListDBHelper;

public class MainActivity extends AppCompatActivity
        implements RVMainAdapter.WishClickListener, LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final int WISH_LOADER_ID = 0;

    private RVMainAdapter adapter;

    RecyclerView recyclerView;

    private FloatingActionButton fab;

    String selection;
    String[] selectionArgs = new String[]{""};
    String sortOrder = WishList.COLUMN_TARGET_DATE;

    Drawer drawer;

    Toolbar mainToolbar;

    TextView toolbarTitle;

    int bucketCount;
    int achievedCount;
    int categoryCount;

    PrimaryDrawerItem achievedDrawerItem;
    SecondaryDrawerItem categoryDrawerItem;
    PrimaryDrawerItem bucketListDrawerItem;
    SecondaryDrawerItem backupDrawerItem;
    SecondaryDrawerItem restoreDrawerItem;
    SecondaryDrawerItem settingsDrawerItem;
    SecondaryDrawerItem cashDrawerItem;
    SecondaryDrawerItem inspirationalDrawerItem;

    AccountHeader headerDrawer;

    GoogleApiClient mGoogleApiClient;


    private String FOLDER_NAME = "BL_BACKUP";

    private String TAG = "bucketlist_backup";

    GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Toast.makeText(getApplicationContext(), getString(R.string.connect_google_drive), Toast.LENGTH_LONG).show();

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

        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbarTitle = (TextView) findViewById(R.id.main_toolbar_title);

        SharedPreferences shre = PreferenceManager.getDefaultSharedPreferences(this);

        shre.registerOnSharedPreferenceChangeListener(this);
        String previouslyEncodedImage = shre.getString("select_toolbar_image", "");

        //image stylization in order to size
        if( !previouslyEncodedImage.equalsIgnoreCase("") ){
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            BitmapDrawable dd = new BitmapDrawable(getResources(), bitmap);
            if(bitmap.getHeight()>=bitmap.getWidth())
                dd.setGravity(Gravity.FILL_HORIZONTAL);
            else
                dd.setGravity(Gravity.FILL);

            Drawable dff = dd;
            mainToolbar.setBackground(dff);
        }

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
        selectionArgs = new String[]{""};

        getSupportLoaderManager().initLoader(WISH_LOADER_ID, null, this);

        headerDrawer = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_drawer_background)
                .build();

        String previouslyEncodedImageDrawer = shre.getString("select_drawer_image", "");

        if( !previouslyEncodedImageDrawer.equalsIgnoreCase("") ){
            byte[] bDrawer = Base64.decode(previouslyEncodedImageDrawer, Base64.DEFAULT);
            Bitmap bitmapDrawer = BitmapFactory.decodeByteArray(bDrawer, 0, bDrawer.length);
            BitmapDrawable dd = new BitmapDrawable(getResources(), bitmapDrawer);
            if(bitmapDrawer.getHeight()>=bitmapDrawer.getWidth())
                dd.setGravity(Gravity.FILL_HORIZONTAL);
            else
                dd.setGravity(Gravity.FILL);

            Drawable dff = dd;
            headerDrawer.setBackground(dff);
        }

        achievedDrawerItem = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.achieved).withIcon(R.drawable.ic_achieved_drawer_item);
        categoryDrawerItem = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.category).withIcon(R.drawable.ic_category_drawer_image);
        bucketListDrawerItem = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.app_name).withIcon(R.drawable.ic_bucket_drawer_item);
        backupDrawerItem  = new SecondaryDrawerItem().withIdentifier(4).withName(R.string.backup).withIcon(R.drawable.ic_backup_drawer_item);
        restoreDrawerItem = new SecondaryDrawerItem().withIdentifier(5).withName(R.string.restore).withIcon(R.drawable.ic_restore_drawer_item);
        settingsDrawerItem = new SecondaryDrawerItem().withIdentifier(6).withName(R.string.settings).withIcon(R.drawable.ic_settings_drawer_item);
        cashDrawerItem = new SecondaryDrawerItem().withIdentifier(7).withName(R.string.cash).withIcon(R.drawable.ic_cash_drawer_item);
        inspirationalDrawerItem = new SecondaryDrawerItem().withIdentifier(8).withName(R.string.inspiration).withIcon(R.drawable.ic_inspire_drawer_item);

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
                        cashDrawerItem,
                        new DividerDrawerItem(),
                        inspirationalDrawerItem,
                        new DividerDrawerItem(),
                        backupDrawerItem,
                        restoreDrawerItem,
                        new DividerDrawerItem(),
                        settingsDrawerItem,
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
                                selectionArgs = new String[]{""};
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
                                selectionArgs = new String[]{""};
                                getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                                fab.setVisibility(View.VISIBLE);
                                break;
                            case 4://backup
                                backup();
                                break;
                            case 5://restore
                                restoreBackup();
                                break;
                            case 6://settings
                                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(settingsIntent);
                                break;
                            case 7://cash
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(getString(R.string.enter_cash));

// Set up the input
                                final EditText input = new EditText(MainActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                                builder.setView(input);

                                final String[] m_Text = {""};
                                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        m_Text[0] = input.getText().toString();
                                        if(!m_Text[0].equals("")){
                                            selection = WishList.COLUMN_PRICE + " <= ?";
                                            selectionArgs = new String[]{m_Text[0]};
                                            getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                                            drawer.closeDrawer();
                                        }
                                    }
                                });
                                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                                break;
                            case 8://inspiration
                                showRandomInspiration();
                                break;
                        }
                        return true;
                    }
                })
                .build();

        refreshBucketAchievedDrawerItem();
        refreshCategoryDrawerItem();

    }

    private void showRandomInspiration(){
        drawer.closeDrawer();
        String[] inspire = getResources().getStringArray(R.array.quotes);
        Random rnd = new Random();
        String randomQuote = inspire[rnd.nextInt(inspire.length)];
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        final TextView out = new TextView(MainActivity.this);
        out.setText(randomQuote);
            out.setTextSize(22);
        out.setPadding(30, 30, 30, 30);
        out.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
        builder.setView(out);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void backup(){
        if (mGoogleApiClient != null) {
            upload_to_drive();
        } else {
            Toast.makeText(this, getString(R.string.couldnt_connect_to_google_manager), Toast.LENGTH_SHORT).show();
        }
    }

    private void upload_to_drive() {
        //async check if folder exists... if not, create it. continue after with create_file_in_folder(driveId);
        check_folder_exists();
    }

    private void check_folder_exists() {
        Query query =
                new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, FOLDER_NAME), Filters.eq(SearchableField.TRASHED, false)))
                        .build();
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override public void onResult(DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Cannot create folder in the root.");
                } else {
                    boolean isFound = false;
                    for (Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(FOLDER_NAME)) {
                            Log.e(TAG, "Folder exists");
                            isFound = true;
                            DriveId driveId = m.getDriveId();
                            create_file_in_folder(driveId);
                            break;
                        }
                    }
                    if (!isFound) {
                        Log.i(TAG, "Folder not found; creating it.");
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(FOLDER_NAME).build();
                        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                .createFolder(mGoogleApiClient, changeSet)
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                                    @Override public void onResult(DriveFolder.DriveFolderResult result) {
                                        if (!result.getStatus().isSuccess()) {
                                            Log.e(TAG, "Error while trying to create the folder");
                                        } else {
                                            Log.i(TAG, "Created a folder");
                                            DriveId driveId = result.getDriveFolder().getDriveId();
                                            create_file_in_folder(driveId);
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    void delete_old_backups(){
        Query query =
                new Query.Builder().addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                        .build();
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override public void onResult(DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Something bad happened");
                } else {
                    for (Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(BucketListDBHelper.DATABASE_NAME)) {

                            DriveId driveId = m.getDriveId();
                            DriveFile oldFile = driveId.asDriveFile();
                            oldFile.delete(mGoogleApiClient);
                        }
                    }
                }
            }
        });
    }

    private void create_file_in_folder(final DriveId driveId) {

        delete_old_backups();

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Error while trying to create new file contents");
                    return;
                }

                OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();

                Toast.makeText(MainActivity.this, getString(R.string.upload), Toast.LENGTH_LONG).show();
                final File theFile = new File(getDatabasePath(BucketListDBHelper.DATABASE_NAME).toString());
                try {
                    FileInputStream fileInputStream = new FileInputStream(theFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e1) {
                    Log.i(TAG, "Unable to write file contents.");
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(theFile.getName()).setMimeType("text/plain").setStarred(false).build();
                DriveFolder folder = driveId.asDriveFolder();
                folder.createFile(mGoogleApiClient, changeSet, driveContentsResult.getDriveContents())
                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                if (!driveFileResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Error while trying to create the file");
                                    return;
                                }
                                Log.v(TAG, "Created a file: " + driveFileResult.getDriveFile().getDriveId());
                            }
                        });
            }
        });
    }


    private void get_file_from_db(DriveId driveId){
        DriveFile df = driveId.asDriveFile();
        downloadFromDrive(df);
}

    private void restoreBackup(){
    Query query =
            new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, BucketListDBHelper.DATABASE_NAME), Filters.eq(SearchableField.TRASHED, false)))
                    .build();
    Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override public void onResult(DriveApi.MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Something bad happened");
            } else {
                boolean isFound = false;
                for (Metadata m : result.getMetadataBuffer()) {
                    if (m.getTitle().equals(BucketListDBHelper.DATABASE_NAME)) {

                        isFound = true;
                        DriveId driveId = m.getDriveId();
                        get_file_from_db(driveId);
                        break;
                    }
                }
            if(!isFound){
                Toast.makeText(MainActivity.this, getString(R.string.no_backups), Toast.LENGTH_SHORT).show();
            }
            }
        }
    });
}

    private void downloadFromDrive(DriveFile file) {
        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            return;
                        }

                        // DriveContents object contains pointers
                        // to the actual byte stream
                        DriveContents contents = result.getDriveContents();
                        InputStream input = contents.getInputStream();
                        try {
                            File file = new File(getDatabasePath(BucketListDBHelper.DATABASE_NAME).toString());
                            OutputStream output = new FileOutputStream(file);
                            try {
                                try {
                                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                    int read;

                                    while ((read = input.read(buffer)) != -1) {
                                        output.write(buffer, 0, read);
                                    }
                                    output.flush();
                                } finally {
                                    output.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                input.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Toast.makeText(getApplicationContext(),getString(R.string.downloading_backup), Toast.LENGTH_LONG).show();

                        getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                    }
                });
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
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, this);
        refreshBucketAchievedDrawerItem();
        refreshCategoryDrawerItem();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {

            // disconnect Google API client connection
            mGoogleApiClient.disconnect();
        }
        super.onPause();
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
                            selectionArgs,
                            sortOrder);

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
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
        if (connectionResult.hasResolution()) {
            try {
                // !!!
                connectionResult.startResolutionForResult(this, 1000);
            } catch (IntentSender.SendIntentException e) {
                Toast.makeText(this, getString(R.string.no_access_to_account), Toast.LENGTH_SHORT).show();
            }}
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case "select_toolbar_image":
                String previouslyEncodedImage = sharedPreferences.getString("select_toolbar_image", "");

                if( !previouslyEncodedImage.equalsIgnoreCase("") ){
                    byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

                    BitmapDrawable dd = new BitmapDrawable(getResources(), bitmap);

                    if(bitmap.getHeight()>=bitmap.getWidth())
                        dd.setGravity(Gravity.FILL_HORIZONTAL);
                    else
                        dd.setGravity(Gravity.FILL);
                    Drawable dff = dd;

                    mainToolbar.setBackground(dff);
                }
                break;
            case "select_drawer_image":
                String previouslyEncodedImageDrawer = sharedPreferences.getString("select_drawer_image", "");

                if( !previouslyEncodedImageDrawer.equalsIgnoreCase("") ){
                    byte[] b = Base64.decode(previouslyEncodedImageDrawer, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

                    BitmapDrawable dd = new BitmapDrawable(getResources(), bitmap);
                    if(bitmap.getHeight()>=bitmap.getWidth())
                        dd.setGravity(Gravity.FILL_HORIZONTAL);
                    else
                        dd.setGravity(Gravity.FILL);

                    Drawable dff = dd;

                    headerDrawer.setBackground(dff);

                }
                break;
        }
    }

    int checkedButton = 3;

    public void onOrderToolbarButton(View view){
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.radiobutton_dialog);


        final List<String> stringList=new ArrayList<>();  // here is list
        stringList.add(getString(R.string.title));
        stringList.add(getString(R.string.category));
        stringList.add(getString(R.string.price));
        stringList.add(getString(R.string.target_date));

        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);

        for(int i=0;i<stringList.size();i++){
            RadioButton rb=new RadioButton(this); // dynamically creating RadioButton and adding to RadioGroup.
            rb.setText(stringList.get(i));
            rb.setTextSize(18);
            rg.addView(rb);
        }

        rg.check(rg.getChildAt(checkedButton).getId());
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        checkedButton = x;
                        String title = btn.getText().toString();
                        switch(title){
                            case "Title":
                                sortOrder = WishList.COLUMN_TITLE;
                                break;
                            case "Category":
                                sortOrder = WishList.COLUMN_CATEGORY;
                                break;
                            case "Price":
                                sortOrder = WishList.COLUMN_PRICE;
                                break;
                            case "Target Date":
                                sortOrder = WishList.COLUMN_TARGET_DATE;
                                break;
                        }
                        getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                        dialog.hide();
                    }
                }
            }
        });

        dialog.show();
    }

    public void onSearchToolbarButton(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.search));

// Set up the input
        final EditText input = new EditText(MainActivity.this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        final String[] m_Text = {""};
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text[0] = input.getText().toString();
                if(!m_Text[0].equals("")){
                    selection = WishList.COLUMN_TITLE + " like ?";
                    selectionArgs = new String[]{"%"+m_Text[0]+"%"};
                    getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                    drawer.closeDrawer();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}

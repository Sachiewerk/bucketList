package ua.aengussong.www.bucketlist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.gson.Gson;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import ua.aengussong.www.bucketlist.backupDrive.Driver_utils;
import ua.aengussong.www.bucketlist.database.BucketListContracts;
import ua.aengussong.www.bucketlist.database.BucketListContracts.WishList;
import ua.aengussong.www.bucketlist.database.BucketListDBHelper;
import ua.aengussong.www.bucketlist.utilities.Utils;

import static ua.aengussong.www.bucketlist.backupDrive.Driver_utils.api;
import static ua.aengussong.www.bucketlist.backupDrive.Driver_utils.contentsOpenedCallback;
import static ua.aengussong.www.bucketlist.backupDrive.Driver_utils.fileCallback;
import static ua.aengussong.www.bucketlist.backupDrive.Driver_utils.mfile;
import static ua.aengussong.www.bucketlist.backupDrive.Driver_utils.preferences_driverId;

public class MainActivity extends AppCompatActivity implements RVMainAdapter.WishClickListener, LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

//    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int WISH_LOADER_ID = 0;
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 10;
    private static final int REQ_CODE_OPEN = 20;
    private static final int DIALOG_ERROR_CODE = 30;

//    ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback;

    /**
     * This is Result result handler of Drive contents.
     * this callback method call CreateFileOnGoogleDrive() method.
     */



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


    GoogleApiClient mGoogleApiClient;

    private int REQUEST_CODE_PICKER = 2;
    private int REQUEST_CODE_SELECT = 3;

    private String FOLDER_NAME = "BL_BACKUP";
    private String FILE_NAME = "bucketList_backup";

//    private Backup backup;
//    private GoogleApiClient mGoogleApiClient;
    private String TAG = "bucketlist_backup";
//    private Button backupButton;
//    private Button restoreButton;
    private IntentSender intentPicker;
//    private Realm realm;


    private static final String GOOGLE_DRIVE_FILE_NAME = "Databackup";
//    boolean fileOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API).
                addScope(Drive.SCOPE_FILE). addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();


  /*      driveContentsCallback =
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {

                        if (result.getStatus().isSuccess()) {
                            if (fileOperation == true){

                                CreateFileOnGoogleDrive(result);

                            }
                        }
                    }
                };*/

        String path = getDatabasePath(BucketListDBHelper.DATABASE_NAME).toString();

        File f = new File(path).getParentFile();
        File file[] = f.listFiles();
        for (File fv:file)
            Toast.makeText(this,fv.getName(), Toast.LENGTH_SHORT).show();


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
        SecondaryDrawerItem restore = new SecondaryDrawerItem().withIdentifier(5).withName("Restore");

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
                        restore,
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
                                break;
                            case 4:
                                backup();
                                break;
                            case 5:
                                restoreBackup();
                                break;
                        }
                        return true;
                    }
                })
                .build();

        refreshBucketAchievedDrawerItem();
        refreshCategoryDrawerItem();

    }

//    private void backup(){
//        Driver_utils.create_backup(this);
//        Gson gson = new Gson();
//        if (Utils.isInternetWorking(this)) {
//            File directorys = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Databackup");
//            if (directorys.exists()) {
//                String json = preferences_driverId.getString("drive_id", "");
//                DriveId driveId = gson.fromJson(json, DriveId.class);
//                //Update file already stored in Drive
//                Driver_utils.trash(driveId, mGoogleApiClient);
//                // Create the Drive API instance
//                Driver_utils.creatBackupDrive(this, mGoogleApiClient);
//                Toast.makeText(getApplicationContext(), "11111", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(getApplicationContext(), "2222", Toast.LENGTH_LONG).show();
//            }
//        } else {
//            Toast.makeText(getApplicationContext(), "333", Toast.LENGTH_LONG).show();
//        }
//    }
    private void backup(){
//

        if (mGoogleApiClient != null) {
            upload_to_drive();
        } else {
            Log.e(TAG, "Could not fucking connect to google drive manager");
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
                                            Log.e(TAG, "U AR A MORON! Error while trying to create the folder");
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
                    Log.e(TAG, "Cannot create folder in the root.");
                } else {
                    for (Metadata m : result.getMetadataBuffer()) {
                        Log.e(TAG, "Folder exists" + "delete_old_backup 1" + m.getTitle() + " " + FILE_NAME + " " + BucketListDBHelper.DATABASE_NAME);
                        if (m.getTitle().equals(BucketListDBHelper.DATABASE_NAME)) {
                            Log.e(TAG, "Folder exists" + "delete_old_backup 2" + m.getTitle() + " " + FILE_NAME + " " + BucketListDBHelper.DATABASE_NAME);

                            DriveId driveId = m.getDriveId();
                            DriveFile oldFile = driveId.asDriveFile();
//                            oldFile.delete(mGoogleApiClient).setResultCallback(deleteCallback);
//                            com.google.android.gms.common.api.Status deleteStatus =
//                                    oldFile.delete(mGoogleApiClient).await();
                                    oldFile.delete(mGoogleApiClient);
//                            Toast.makeText(MainActivity.this, deleteStatus.getStatus().toString(), Toast.LENGTH_SHORT).show();
                            /*if (!deleteStatus.isSuccess()) {
                                Toast.makeText(MainActivity.this, "delete not wishlo", Toast.LENGTH_SHORT).show();
                            }*/

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
                    Log.e(TAG, "U AR A MORON! Error while trying to create new file contents");
                    return;
                }

                OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();

                //------ THIS IS AN EXAMPLE FOR PICTURE ------
                //ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                //image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                //try {
                //  outputStream.write(bitmapStream.toByteArray());
                //} catch (IOException e1) {
                //  Log.i(TAG, "Unable to write file contents.");
                //}
                //// Create the initial metadata - MIME type and title.
                //// Note that the user will be able to change the title later.
                //MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                //    .setMimeType("image/jpeg").setTitle("Android Photo.png").build();

                //------ THIS IS AN EXAMPLE FOR FILE --------
                Toast.makeText(MainActivity.this, "Uploading to drive. If you didn't fucked up something like usual you should see it there", Toast.LENGTH_LONG).show();
                final File theFile = new File(getDatabasePath(BucketListDBHelper.DATABASE_NAME).toString()); //>>>>>> WHAT FILE ?
                try {
                    FileInputStream fileInputStream = new FileInputStream(theFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e1) {
                    Log.i(TAG, "U AR A MORON! Unable to write file contents.");
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(theFile.getName()).setMimeType("text/plain").setStarred(false).build();
                DriveFolder folder = driveId.asDriveFolder();
                folder.createFile(mGoogleApiClient, changeSet, driveContentsResult.getDriveContents())
                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                if (!driveFileResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "U AR A MORON!  Error while trying to create the file");
                                    return;
                                }
                                Log.v(TAG, "Created a file: " + driveFileResult.getDriveFile().getDriveId());
                            }
                        });
            }
        });
    }


    private void openFolderPicker() {
        try {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                if (intentPicker == null)
                    intentPicker = buildIntent();
                //Start the picker to choose a folder
                startIntentSenderForResult(
                        intentPicker, REQUEST_CODE_PICKER, null, 0, 0, 0);
            }
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Unable to send intent", e);
        }
    }

    private IntentSender buildIntent() {
        return Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{DriveFolder.MIME_TYPE})
                .build(mGoogleApiClient);
    }


void get_file_from_db(DriveId driveId){
    DriveFile df = driveId.asDriveFile();
    downloadFromDrive(df);
/*    df.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
            .setResultCallback(contentsOpenedCallback);

    ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        // display an error saying file can't be opened
                        return;
                    }
                    // DriveContents object contains pointers
                    // to the actual byte stream
                    DriveContents contents = result.getDriveContents();
                }
            };*/
}

private void restoreBackup(){
    Log.e(TAG, "Folder exists restore backup 1");
    Query query =
            new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, BucketListDBHelper.DATABASE_NAME), Filters.eq(SearchableField.TRASHED, false)))
                    .build();
    Log.e(TAG, "Folder exists restore backup 2");
    Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override public void onResult(DriveApi.MetadataBufferResult result) {
            Log.e(TAG, "Folder exists restore backup 3");
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Cannot create folder in the root.");
                Log.e(TAG, "Folder exists restore backup 4");
            } else {
                Log.e(TAG, "Folder exists restore backup5 ");
                boolean isFound = false;
                Log.e(TAG, "Folder exists restore backup6 ");
                for (Metadata m : result.getMetadataBuffer()) {
                    Log.e(TAG, "Folder exists restore backup 7"+m.getTitle()+" "+FILE_NAME + " " + BucketListDBHelper.DATABASE_NAME);
                    if (m.getTitle().equals(BucketListDBHelper.DATABASE_NAME)) {

                        isFound = true;
                        DriveId driveId = m.getDriveId();
                        get_file_from_db(driveId);
                        break;
                    }
                }
            if(!isFound){
                Toast.makeText(MainActivity.this, "NO BACKUPS available", Toast.LENGTH_SHORT).show();
            }
            }
        }
    });
}

 /*   private void restoreBackup(){
//        File dbFile = new File(getDatabasePath(BucketListDBHelper.DATABASE_NAME).toString());
//        DriveFile file = dbFile;

        //*//*openFilePicker();
        ResultCallback<DriveApi.DriveIdResult> idCallback = new ResultCallback<DriveApi.DriveIdResult>() {
//            @Override
            public void onResult(@NonNull DriveApi.DriveIdResult driveIdResult) {
                DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, driveIdResult.getDriveId());
                PendingResult<DriveApi.DriveContentsResult> pendingResult = file.open(mGoogleApiClient,
                        DriveFile.MODE_READ_ONLY, null);

                pendingResult.setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    public String fileAsString;
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        DriveContents fileContents = result.getDriveContents();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(fileContents.getInputStream()));
                        StringBuilder builder = new StringBuilder();
                        String oneLine;
                        Log.i(TAG, "reading input stream and building string...");
                        try {
                            while ((oneLine = reader.readLine()) != null) {
                                builder.append(oneLine);
                            }
                            fileAsString = builder.toString();
                        } catch (IOException e) {
                            Log.e(TAG, "IOException while reading from the stream", e);
                        }
                        fileContents.discard(mGoogleApiClient);
                        Intent intent = new Intent(RetrieveContentsActivity.this,
                                DisplayFileActivity.class);
                        intent.putExtra("text", fileAsString);
                        startActivity(intent);
                    }
                });
            }

            }
        };

//        downloadFromDrive();
    }*/

    private void openFilePicker() {
        //        build an intent that we'll use to start the open file activity
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
//                these mimetypes enable these folders/files types to be selected
                .setMimeType(new String[]{DriveFolder.MIME_TYPE, "text/plain"})
                .build(mGoogleApiClient);
        try {
            startIntentSenderForResult(
                    intentSender, REQUEST_CODE_SELECT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Unable to send intent", e);
        }
    }

    private void downloadFromDrive(DriveFile file) {
        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Toast.makeText(MainActivity.this, "1", Toast.LENGTH_SHORT).show();
                            return;

                        }

                        // DriveContents object contains pointers
                        // to the actual byte stream
                        DriveContents contents = result.getDriveContents();
                        InputStream input = contents.getInputStream();
                        Toast.makeText(MainActivity.this, "2", Toast.LENGTH_SHORT).show();
                        try {
                            File file = new File(getDatabasePath(BucketListDBHelper.DATABASE_NAME).toString());
                            OutputStream output = new FileOutputStream(file);
                            Toast.makeText(MainActivity.this, "3", Toast.LENGTH_SHORT).show();
                            try {
                                try {
                                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                                    int read;

                                    while ((read = input.read(buffer)) != -1) {
                                        output.write(buffer, 0, read);
                                    }
                                    Toast.makeText(MainActivity.this, "4", Toast.LENGTH_SHORT).show();
                                    output.flush();
                                } finally {
                                    output.close();
                                    Toast.makeText(MainActivity.this, "5", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } finally {
                            Toast.makeText(MainActivity.this, "6", Toast.LENGTH_SHORT).show();
                            /*try {
                                input.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/
                        }

                        Toast.makeText(getApplicationContext(),"activity_backup_drive_message_restart", Toast.LENGTH_LONG).show();

                        // Reboot app
                        /*Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        System.exit(0);*/
                        getSupportLoaderManager().restartLoader(WISH_LOADER_ID, null, MainActivity.this);
                    }
                });
    }

    private void uploadToDrive(DriveId mFolderDriveId) {
        if (mFolderDriveId != null) {
            //Create the file on GDrive
            final DriveFolder folder = mFolderDriveId.asDriveFolder();
            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(DriveApi.DriveContentsResult result) {
                            if (!result.getStatus().isSuccess()) {
                                Log.e(TAG, "Error while trying to create new file contents");
                                return;
                            }
                            final DriveContents driveContents = result.getDriveContents();

                            // Perform I/O off the UI thread.
                            new Thread() {
                                @Override
                                public void run() {
                                    // write content to DriveContents
                                    OutputStream outputStream = driveContents.getOutputStream();

                                    FileInputStream inputStream = null;
                                    try {
                                        inputStream = new FileInputStream(new File(getDatabasePath(BucketListDBHelper.DATABASE_NAME).toString()));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }

                                    byte[] buf = new byte[1024];
                                    int bytesRead;
                                    try {
                                        if (inputStream != null) {
                                            while ((bytesRead = inputStream.read(buf)) > 0) {
                                                outputStream.write(buf, 0, bytesRead);
                                            }
                                        }
                                    } catch (IOException e) {

                                        e.printStackTrace();
                                    }


                                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                            .setTitle("bucketList_backup")
                                            .setMimeType("text/plain")
                                            .build();

                                    // create a file in selected folder
                                    folder.createFile(mGoogleApiClient, changeSet, driveContents)
                                            .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                                @Override
                                                public void onResult(DriveFolder.DriveFileResult result) {
                                                    if (!result.getStatus().isSuccess()) {
                                                        Log.d(TAG, "Error while trying to create the file");

                                                        finish();
                                                        return;
                                                    }

//                                                    finish();
                                                }
                                            });
                                }
                            }.start();
                        }
                    });
        }
    }

  /*  private void restoreBackup(){

        // Launch user interface and allow user to select file
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{"application/zip"})
                .build(mGoogleApiClient);
        try {

            startIntentSenderForResult(

                    intentSender, REQ_CODE_OPEN, null, 0, 0, 0);

        } catch (IntentSender.SendIntentException e) {

            Log.w(TAG, e.getMessage());
        }
//        fileOperation = true;
        // create new contents resource
*//*        final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {

                        if (result.getStatus().isSuccess()) {
                            if (fileOperation == true){

                                CreateFileOnGoogleDrive(result);

                            }
                        }
                    }
                };*//*

//*//*        Drive.DriveApi.newDriveContents(mGoogleApiClient)
//                .setResultCallback(driveContentsCallback);*//*


    }*/

/*    public void CreateFileOnGoogleDrive(DriveApi.DriveContentsResult result){

        final DriveContents driveContents = result.getDriveContents();

        // Perform I/O off the UI thread.
        new Thread() {
            @Override
            public void run() {
                // write content to DriveContents
                OutputStream outputStream = driveContents.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);
                try {
                    writer.write("Hello abhay!");
                    writer.close();

                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle("abhaytest2")
                    .setMimeType("text/plain")
                    .setStarred(true).build();

                // create a file in root folder
                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                        .createFile(mGoogleApiClient, changeSet, driveContents)
                .setResultCallback(fileCallback);
            }
        }.start();
    }*/

  /*  *//**
     * Handle result of Created file
     *//*
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
    ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if (result.getStatus().isSuccess()) {

                Toast.makeText(getApplicationContext(), "file created: "+" "+
                        result.getDriveFile().getDriveId(), Toast.LENGTH_LONG).show();

            }

            return;

        }
    };*/

   /* final private ResultCallback<DriveApi.DriveContentsResult> contentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {

        @Override
        public void onResult(DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.v(TAG, "Error while trying to create new file contents");
                return;
            }

            String mimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType("db");
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle("bucketlistbackup") // Google Drive File name
                    .setMimeType(mimeType)
                    .setStarred(true).build();
            // create a file on root folder
            Drive.DriveApi.getRootFolder(api)
                    .createFile(api, changeSet, result.getDriveContents())
                    .setResultCallback(fileCallback);
        }

    };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new ResultCallback<DriveFolder.DriveFileResult>() {

        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.v(TAG, "Error while trying to create the file");
                return;
            }
            mfile = result.getDriveFile();
            mfile.open(api, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(contentsOpenedCallback);
        }
    };

    final private ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback = new ResultCallback<DriveApi.DriveContentsResult>() {

        @Override
        public void onResult(DriveApi.DriveContentsResult result) {

            if (!result.getStatus().isSuccess()) {
                Log.v(TAG, "Error opening file");
                return;
            }

            try {
                FileInputStream is = new FileInputStream(getDbPath());
                BufferedInputStream in = new BufferedInputStream(is);
                byte[] buffer = new byte[8 * 1024];
                DriveContents content = result.getDriveContents();
                BufferedOutputStream out = new BufferedOutputStream(content.getOutputStream());
                int n = 0;
                while( ( n = in.read(buffer) ) > 0 ) {
                    out.write(buffer, 0, n);
                }

                in.close();
                content.commit(api,null).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
//                commitAndCloseContents is DEPRECATED -->*//**mfile.commitAndCloseContents(api, content).setResultCallback(new ResultCallback<Status>() {
//                 @Override
//                 public void onResult(Status result) {
//                 // Handle the response status
//                 }
//                 });**//*
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    };*/

    private File getDbPath() {
        return this.getDatabasePath(BucketListDBHelper.DATABASE_NAME);
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
/*        if(mGoogleApiClient.isConnected())
            Toast.makeText(this, "true", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "false", Toast.LENGTH_SHORT).show();*/
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

    public void writeToFile(String fileName, String body) {
        FileOutputStream fos = null;
        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/xtests/");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("ALERT", "U AR A MORON!  could not create the directories. CHECK THE FUCKING PERMISSIONS SON!");
                }
            }
            final File myFile = new File(dir, fileName + "_" + String.valueOf(System.currentTimeMillis()) + ".txt");
            if (!myFile.exists()) {
                myFile.createNewFile();
            }

            fos = new FileOutputStream(myFile);
            fos.write(body.getBytes());
            fos.close();
            Toast.makeText(MainActivity.this, "File created ok! Let me give you a fucking congratulations!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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







    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
//        Drive.DriveApi.newDriveContents(api).setResultCallback(contentsCallback);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }


    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DIALOG_ERROR_CODE) {
            if (resultCode == RESULT_OK) { // Error was resolved, now connect to the client if not done so.
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }

        }
        if (requestCode == REQ_CODE_OPEN && resultCode == RESULT_OK) {
            DriveId mSelectedFileDriveId = data.getParcelableExtra(
                    OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
            Log.e("DriveID ---", mSelectedFileDriveId + "");
            Gson gson = new Gson();
            String json = gson.toJson(mSelectedFileDriveId); // myObject - instance of MyObject
            SharedPreferences.Editor editor_drive = preferences_driverId.edit();
            editor_drive.putString("drive_id", json).commit();
            Log.e(TAG, "driveId this 1-- " + mSelectedFileDriveId);
            if (Utils.isInternetWorking(this)) {
                //restore Drive file to SDCArd
                Driver_utils.restoreDriveBackup(this, mGoogleApiClient, GOOGLE_DRIVE_FILE_NAME, preferences_driverId, mfile);
                Driver_utils.restore(this);

            } else {
                Toast.makeText(getApplicationContext(), "no internt", Toast.LENGTH_LONG).show();
            }
        }
        if (resultCode == RESOLVE_CONNECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }

    }*/


    protected static final int REQUEST_CODE_RESOLUTION = 1337;
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
                }
                break;
            // REQUEST_CODE_PICKER
            case 2:
//                intentPicker = null;
                Toast.makeText(this, "2", Toast.LENGTH_SHORT).show();
//
//                if (resultCode == RESULT_OK) {
//                    //Get the folder drive id
//                    DriveId mFolderDriveId = data.getParcelableExtra(
//                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
//
//                    uploadToDrive(mFolderDriveId);
//                }
                break;

            // REQUEST_CODE_SELECT
            case 3:
             /*   if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "3", Toast.LENGTH_SHORT).show();
                    // get the selected item's ID
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    DriveFile file = driveId.asDriveFile();
                    downloadFromDrive(file);

                } else {

                }
                finish();*/
                break;

        }
    }



}

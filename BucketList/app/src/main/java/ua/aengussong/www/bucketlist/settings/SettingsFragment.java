package ua.aengussong.www.bucketlist.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import ua.aengussong.www.bucketlist.R;

import static android.app.Activity.RESULT_OK;

/**
 * Created by coolsmileman on 10.06.2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    int PICK_IMAGE = 1;
    int PICK_DRAWER_IMAGE = 2;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_visualizer);

        Preference preference = findPreference("select_toolbar_image");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                return true;
            }
        });

        Preference preferenceDrawer = findPreference("select_drawer_image");
        preferenceDrawer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preferenceDrawer) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_DRAWER_IMAGE);
                return true;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        try {
            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && imageReturnedIntent != null) {
                selectImage("select_toolbar_image", imageReturnedIntent);
            } else
                if (requestCode == PICK_DRAWER_IMAGE && resultCode == RESULT_OK && imageReturnedIntent != null) {
                selectImage("select_drawer_image", imageReturnedIntent);
            } else{
                Toast.makeText(getActivity(), getString(R.string.havent_picked_image), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), getString(R.string.bad), Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage(String prefName, Intent imageReturnedIntent){
        Uri selectedImage = imageReturnedIntent.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String imgDecodableString = cursor.getString(columnIndex);
        cursor.close();
        Bitmap galleryImage = BitmapFactory.decodeFile(imgDecodableString);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nh = (int) ( galleryImage.getHeight() * (512.0 / galleryImage.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(galleryImage, 512, nh, true);
        scaled.compress(Bitmap.CompressFormat.JPEG, 50, baos);

        byte[] b = baos.toByteArray();

        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        SharedPreferences shre = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = shre.edit();
        edit.putString(prefName, encodedImage);
        edit.commit();

        Toast.makeText(getActivity(), getString(R.string.changes_applied), Toast.LENGTH_SHORT).show();
    }
}

package com.project.contacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DetailsActivity extends AppCompatActivity {

    Contact c;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent i = getIntent();
        c = new Contact(i.getStringExtra("name"), i.getStringExtra("number"),
                i.getStringExtra("uri"), i.getStringExtra("cid"));
        TextView name = findViewById(R.id.name);
        name.setText(c.getName());
        TextView number = findViewById(R.id.number);
        number.setText(c.getPh_no());
        if (c.getPfp_uri() != null) {
            ImageView pfp = findViewById(R.id.pfp);
            pfp.setPadding(0, 0, 0, 0);
            pfp.setImageURI(Uri.parse(c.getPfp_uri()));
        }
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener((View) -> finish());
        ImageView copy = findViewById(R.id.copy);
        copy.setOnClickListener((View v) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("contact number", c.getPh_no());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Phone Number Copied", Toast.LENGTH_SHORT).show();
        });
        ImageView call = findViewById(R.id.call);
        call.setOnClickListener((View v) -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 20);
            else {
                Intent caller = new Intent(Intent.ACTION_CALL);
                caller.setData(Uri.parse("tel:" + c.getPh_no()));
                startActivity(caller);
            }
        });
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        String[] projection = new String[]{
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                        };
                        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                        String[] selectionArgs = new String[]{c.getContact_id()};
                        Cursor cursor = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                projection,
                                selection,
                                selectionArgs,
                                null);
                        if (cursor != null && cursor.moveToFirst()) {
                            @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            @SuppressLint("Range") String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            cursor.close();
                            name.setText(displayName);
                            number.setText(phoneNumber);
                            c = new Contact(displayName, phoneNumber, c.getPfp_uri(), c.getContact_id());
                        } else {
                            finish();
                        }
                    }
                });
        TextView edit = findViewById(R.id.edit);
        edit.setOnClickListener((View v) -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 30);
                return;
            }
            Intent editor = new Intent(Intent.ACTION_EDIT);
            Uri content_uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(c.getContact_id()));
            editor.setData(content_uri);
            editor.putExtra("finishActivityOnSaveCompleted", true);
            activityResultLauncher.launch(editor);
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 20) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent caller = new Intent(Intent.ACTION_CALL);
                caller.setData(Uri.parse("tel:" + c.getPh_no()));
                startActivity(caller);
            } else
                Toast.makeText(getApplicationContext(), "Permission denied, can't make phone call", Toast.LENGTH_LONG).show();
        } else if (requestCode == 30) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent editor = new Intent(Intent.ACTION_EDIT);
                Uri content_uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(c.getContact_id()));
                editor.setData(content_uri);
                editor.putExtra("finishActivityOnSaveCompleted", true);
                activityResultLauncher.launch(editor);
            } else
                Toast.makeText(getApplicationContext(), "Permission denied, can't edit contact", Toast.LENGTH_LONG).show();
        }

    }
}
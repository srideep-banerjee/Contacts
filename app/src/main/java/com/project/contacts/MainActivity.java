package com.project.contacts;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    TextView msgBox;
    SearchView searchView;
    RecyclerView recyclerView;
    ContactsAdapter contactsAdapter;
    ArrayList<Contact> allContacts;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener((View v) -> {
            Intent i = new Intent(Intent.ACTION_INSERT);
            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
            i.putExtra("finishActivityOnSaveCompleted", true);
            startActivity(i);
        });
        msgBox = findViewById(R.id.MsgBox);
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsAdapter = new ContactsAdapter(new ArrayList<>(),this);
        recyclerView.setAdapter(contactsAdapter);
        loadContacts();
        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    String s = searchView.getQuery().toString();
                    loadContacts();
                    search(s);
                });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 10) return;
        if (permissions.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            loadContacts();
        else
            this.finish();
    }

    public void launchDetailsActivityForResult(Intent i) {
        activityResultLauncher.launch(i);
    }

    public void search(String key) {
        if (allContacts == null) return;
        int[] bounds = new Searcher(allContacts).search(key);
        if (bounds == null) {
            msgBox.setText("No Search Results !");
            msgBox.setVisibility(View.VISIBLE);
            return;
        } else if (msgBox.getVisibility() == View.VISIBLE)
            msgBox.setVisibility(View.INVISIBLE);
        contactsAdapter.changeData(allContacts.subList(bounds[0], bounds[1] + 1));
    }

    public void loadContacts() {
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS}, 10);
            return;
        }
        final AccountManager accountManager = AccountManager.get(this);
        msgBox.setVisibility(View.VISIBLE);
        searchView.setEnabled(false);
        recyclerView.setVisibility(View.INVISIBLE);
        new Thread() {
            @Override
            public void run() {
                String selection = null;
                Account[] accounts = accountManager.getAccounts();
                if (accounts.length != 0) {
                    StringBuilder selectionBuilder = new StringBuilder();
                    selectionBuilder
                            .append(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET)
                            .append("!='")
                            .append(accounts[0].type)
                            .append("'");
                    for (int i = 1; i < accounts.length; i++) {
                        selectionBuilder
                                .append(" AND ")
                                .append(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET)
                                .append("!='")
                                .append(accounts[i].type)
                                .append("'");
                    }
                    selection = selectionBuilder.toString();
                }
                Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, selection, null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC");
                ArrayList<Contact> contact_data = new ArrayList();
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String num = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    @SuppressLint("Range") String pfp_uri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                    @SuppressLint("Range") String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    contact_data.add(new Contact(name, num, pfp_uri, contact_id));
                }
                cursor.close();
                allContacts = contact_data;
                runOnUiThread(() -> {
                    contactsAdapter.changeData(contact_data);
                    msgBox.setVisibility(View.INVISIBLE);
                    searchView.setEnabled(true);
                    recyclerView.setVisibility(View.VISIBLE);
                });
            }
        }.start();

    }
}
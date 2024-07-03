package com.project.contacts;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class ContactRepository {
    private final ContentResolver contentResolver;
    private final String externalContactSelection;
    private String searchText = "";

    public ContactRepository(ContentResolver contentResolver, String externalContactSelection) {
        this.contentResolver = contentResolver;
        if (externalContactSelection.equals("")) externalContactSelection = null;
        this.externalContactSelection = externalContactSelection;
    }

    public Single<List<Contact>> loadData(int pageNumber, int pageLength) {
        Log.d("CONTACTS_LOGS","Loading page -> {Page Number: " + pageNumber + ", Page Length: " + pageLength + "}");
        return Single.create(emitter -> {
            int offset = pageNumber * PagingConstants.pageSize;
            String selection = externalContactSelection + " AND " +ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+ " LIKE ?";
            String searchQuery = searchText + "%";
            ArrayList<Contact> contact_data = new ArrayList<>();
            try (Cursor cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                    },
                    selection,
                    new String[]{ searchQuery },
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC" + " LIMIT " + pageLength + " OFFSET " + offset
            )) {
                if (cursor == null) {
                    emitter.onError(new Exception("Database cursor is null"));
                    return;
                }
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String num = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    @SuppressLint("Range") String pfp_uri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                    @SuppressLint("Range") String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    contact_data.add(new Contact(name, num, pfp_uri, contact_id));
                }
            } catch (Exception e) {
                Log.e("CONTACT_ERROR", e.toString());
            }
            Log.d("CONTACTS_LOGS","Record count = " + contact_data.size());
            emitter.onSuccess(contact_data);
        });
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}

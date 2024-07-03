package com.project.contacts;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ContactRepository {
    private final ContentResolver contentResolver;
    private final String externalContactSelection;

    public ContactRepository(ContentResolver contentResolver, String externalContactSelection) {
        this.contentResolver = contentResolver;
        if (externalContactSelection.equals("")) externalContactSelection = null;
        this.externalContactSelection = externalContactSelection;
    }

    public Single<List<Contact>> loadData(int pageNumber, int pageLength) {
        Log.d("CONTACTS_LOGS","Loading page -> {Page Number: " + pageNumber + ", Page Length: " + pageLength + "}");
        return Single.create(emitter -> {
            int offset = pageNumber * pageLength;
            String selection = externalContactSelection;
            ArrayList<Contact> contact_data = new ArrayList<>();
            try (Cursor cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI, ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
                    selection,
                    null,
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
            }
            emitter.onSuccess(contact_data);
        });
    }
}

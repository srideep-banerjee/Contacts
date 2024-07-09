package com.project.contacts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    SearchView searchView;
    RecyclerView recyclerView;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ContactViewModel contactViewModel;
    ContactAdapter contactAdapter;
    CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        contactAdapter = new ContactAdapter(new ContactComparator(), (contact)-> {
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra("name", contact.getName());
            i.putExtra("number", contact.getPh_no());
            i.putExtra("uri", contact.getPfp_uri());
            i.putExtra("cid", contact.getContact_id());
            launchDetailsActivityForResult(i);
        });

        recyclerView.setAdapter(contactAdapter);

        compositeDisposable = new CompositeDisposable();

        if (hasNecessaryPermissions()) initPaging();
        else requestNecessaryPermissions();

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
                    //loadContacts();
                    search(s);
                });

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener((View v) -> {
            Intent i = new Intent(Intent.ACTION_INSERT);
            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
            i.putExtra("finishActivityOnSaveCompleted", true);
            startActivity(i);
        });
        final TextChangeDelayedExecutor delayedExecutor = new TextChangeDelayedExecutor(
                "",
                this::search
        );
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                delayedExecutor.update(s);
                return false;
            }
        });
    }

    public boolean hasNecessaryPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestNecessaryPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS}, 10);
    }

    public void initPaging() {
        Disposable singleDisposable = contactViewModel
                .getContactPageDataFlowable()
                .observeOn(Schedulers.computation())
                .subscribe((pagingDataFlowable)->{
                    Disposable flowableDisposable = pagingDataFlowable
                            .subscribe((pagingData) -> {
                                contactAdapter.submitData(this.getLifecycle(), pagingData);
                            });
                    compositeDisposable.add(flowableDisposable);
                });
        compositeDisposable.add(singleDisposable);
    }

    public void initializeViews() {
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 10) return;
        if (permissions.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            initPaging();
        else
            this.finish();
    }

    public void launchDetailsActivityForResult(Intent i) {
        activityResultLauncher.launch(i);
    }

    public void search(String key) {
        contactViewModel.setSearchText(key);
        contactAdapter.refresh();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }
}
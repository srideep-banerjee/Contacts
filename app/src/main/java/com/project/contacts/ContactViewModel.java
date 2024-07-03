package com.project.contacts;

import static io.reactivex.rxjava3.core.Single.create;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Function;
import kotlinx.coroutines.CoroutineScope;

public class ContactViewModel extends AndroidViewModel {
    private final Single<Flowable<PagingData<Contact>>> contactPagingDataFlowable;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        this.contactPagingDataFlowable = Single.<Flowable<PagingData<Contact>>>create(emitter -> {
            String selection = getExternalContactSelection()
                    .observeOn(Schedulers.io())
                    .blockingGet();

            ContactRepository repository = new ContactRepository(contentResolver(), selection);
            CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
            Pager<Integer, Contact> pager = new Pager<>(
                    new PagingConfig(10, 20, true, 30, 50),
                    ()-> new ContactPagingSource(repository)
            );
            Flowable<PagingData<Contact>> flowable = PagingRx.getFlowable(pager);
            PagingRx.cachedIn(flowable, viewModelScope);
            emitter.onSuccess(flowable);
        }).subscribeOn(Schedulers.io()).cache();
    }

    public Single<Flowable<PagingData<Contact>>> getContactPageDataFlowable() {
        return contactPagingDataFlowable;
    }

    private ContentResolver contentResolver() {
        return getApplication().getContentResolver();
    }

    private Single<String> getExternalContactSelection() {
        return Single.<String>create(emitter -> {
            AccountManager accountManager = AccountManager.get(this.getApplication());
            String selection = "";
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
            emitter.onSuccess(selection);
        }).subscribeOn(Schedulers.computation());

    }
}

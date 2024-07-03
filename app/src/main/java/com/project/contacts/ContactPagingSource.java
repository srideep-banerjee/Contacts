package com.project.contacts;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ContactPagingSource extends RxPagingSource<Integer, Contact> {
    private final ContactRepository repository;

    ContactPagingSource(ContactRepository repository) {
        this.repository = repository;
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Contact> pagingState) {
        Integer anchorPosition = pagingState.getAnchorPosition();
        Log.d("CONTACTS_LOGS", "Anchor Position: " + anchorPosition);
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, Contact> anchorPage = pagingState.closestPageToPosition(anchorPosition);

        Log.d("CONTACTS_LOGS", "Anchor Page == null: " + (anchorPage == null));
        if (anchorPage == null) {
            return null;
        }

        Integer prevKey = anchorPage.getPrevKey();
        Log.d("CONTACTS_LOGS", "Previous key: " + prevKey);
        if (prevKey != null) {
            return prevKey + 1;
        }

        Integer nextKey = anchorPage.getNextKey();
        Log.d("CONTACTS_LOGS", "Next key: " + nextKey);
        if (nextKey != null) {
            return nextKey - 1;
        }

        return null;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, Contact>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        final int pageNumber = loadParams.getKey() != null ? loadParams.getKey() : 0;
        final int nextKey = pageNumber + loadParams.getLoadSize() / PagingConstants.pageSize;
        final int prevKey = pageNumber - 1;
        Log.d("CONTACTS_LOGS", "Loading page: " + pageNumber);
        Single<List<Contact>> single = repository.loadData(pageNumber, loadParams.getLoadSize());

        Single<LoadResult<Integer, Contact>> mappedSingle =  single
                .subscribeOn(Schedulers.io())
                .map(data -> new LoadResult.Page<>(
                        data,
                        pageNumber == 0 ? null : prevKey,
                        data.isEmpty() ? null : nextKey
                ));
        return mappedSingle.onErrorReturn(LoadResult.Error::new);
    }
}

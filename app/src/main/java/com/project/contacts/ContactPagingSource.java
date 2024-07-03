package com.project.contacts;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.coroutines.Continuation;

public class ContactPagingSource extends RxPagingSource<Integer, Contact> {
    private final ContactRepository repository;

    ContactPagingSource(ContactRepository repository) {
        this.repository = repository;
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Contact> pagingState) {
        Integer anchorPosition = pagingState.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, Contact> anchorPage = pagingState.closestPageToPosition(anchorPosition);
        if (anchorPage == null) {
            return null;
        }

        Integer prevKey = anchorPage.getPrevKey();
        if (prevKey != null) {
            return prevKey + pagingState.getConfig().pageSize;
        }

        Integer nextKey = anchorPage.getNextKey();
        if (nextKey != null) {
            return nextKey - pagingState.getConfig().pageSize;
        }

        return null;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, Contact>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        final int pageNumber = loadParams.getKey() != null ? (loadParams.getKey() / loadParams.getLoadSize()) : 0;
        final int nextKey = (pageNumber + 1) * loadParams.getLoadSize();
        final int prevKey = (pageNumber - 1) * loadParams.getLoadSize();
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

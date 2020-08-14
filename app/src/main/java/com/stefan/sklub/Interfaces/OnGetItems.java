package com.stefan.sklub.Interfaces;

public interface OnGetItems<T> {
    void onGetItem(T item);
    void onFinishedGettingItems();
}

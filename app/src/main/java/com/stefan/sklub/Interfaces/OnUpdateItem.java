package com.stefan.sklub.Interfaces;

import com.stefan.sklub.Model.User;

public interface OnUpdateItem<T> {
    void onSuccessfulUpdate(User newUserData);
    void onError(String error);
}

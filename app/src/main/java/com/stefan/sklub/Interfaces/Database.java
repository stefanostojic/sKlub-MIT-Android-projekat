package com.stefan.sklub.Interfaces;

import com.stefan.sklub.Model.Event;

public interface Database {
    void addEvent(Event event);
    void getEvents(OnComplete<Event> callback);
}

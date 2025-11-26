package com.ticketshall.events.dtos.responses;

import lombok.AllArgsConstructor;

import java.util.List;

public class ListResponse<T> {

    // a response will return a list of data
    // we don't care about the entity name of it
    public List<T> data;

    // the total items in db for pagination usage
    public int total;

    public ListResponse(List<T> data, int total) {
        this.data = data;
        this.total = total;
    }
}

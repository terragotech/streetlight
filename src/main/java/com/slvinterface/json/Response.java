package com.slvinterface.json;

import java.util.ArrayList;
import java.util.List;

public class Response {
    private List<Filter> filters = new ArrayList<>();
    private int statusCode;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "Response{" +
                "filters=" + filters +
                ", statusCode=" + statusCode +
                '}';
    }
}

package com.example.campusstudyroombookingsystem.model;

import java.util.List;

public class PageResponse<T> {
    private List<T> records;
    private Long total;
    private Long current;
    private Long size;

    public List<T> getRecords() { return records; }
    public Long getTotal() { return total; }
    public Long getCurrent() { return current; }
    public Long getSize() { return size; }
}

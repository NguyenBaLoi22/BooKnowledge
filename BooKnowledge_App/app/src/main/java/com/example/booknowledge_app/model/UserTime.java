package com.example.booknowledge_app.model;

public class UserTime {


    private String name;
    private long lastActiveTimestamp;

    public UserTime() {
        // Hàm khởi tạo rỗng để sử dụng với Firebase Realtime Database
    }

    public UserTime(String name, long lastActiveTimestamp) {
        this.name = name;
        this.lastActiveTimestamp = lastActiveTimestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }

    public void setLastActiveTimestamp(long lastActiveTimestamp) {
        this.lastActiveTimestamp = lastActiveTimestamp;
    }
}



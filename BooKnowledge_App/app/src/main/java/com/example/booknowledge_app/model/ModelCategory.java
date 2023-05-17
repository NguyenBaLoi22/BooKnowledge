package com.example.booknowledge_app.model;

public class ModelCategory {
    String id, category,uid;
    String timestamp;
    public ModelCategory(){
    }

    public ModelCategory(String id, String category, String uid, String timestamp) {
        this.id = id;
        this.category = category;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getUid() {
        return uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

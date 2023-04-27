package com.example.booknowledge_app.model;

public class ModelPdf {
    //variables
    String uid,id,title,description,author,categoryId,url,timestamp;

    //constructor
    //empty constructor, required for firebase
    public ModelPdf(){

    }

    public ModelPdf(String uid, String id, String title, String description, String author, String categoryId, String url, String timestamp) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
    }

    //getter , setter

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

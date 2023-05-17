package com.example.booknowledge_app.model;

public class ModelPdf {
    //variables
    String uid, id, title, description, author, categoryId, url, timestamp;
    long viewsCount, downloadCount;
    boolean favorite;

    //constructor
    public ModelPdf() {//empty constructor, required for firebase
    }

    public ModelPdf(String uid, String id, String title, String description, String author, String categoryId, String url, String timestamp, long viewsCount, long downloadCount, boolean favorite) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.categoryId = categoryId;
        this.url = url;
        this.timestamp = timestamp;
        this.viewsCount = viewsCount;
        this.downloadCount = downloadCount;
        this.favorite = favorite;
    }

    //getter , setter


    public String getUid() {
        return uid;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getUrl() {
        return url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}

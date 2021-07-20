package com.example.baru_app.POST_HOME;

import com.google.firebase.firestore.DocumentId;

public class Post_Model {
    private String title;
    private String date;
    private String description;
    private String numberComment;
    private String time;
    private String author_id;
    private String docID;


    public Post_Model(){

    }


    @DocumentId
    public String getDocID() {
        return docID;
    }



    public Post_Model(String Title, String Date, String Description, String NumberComment,String Time, String Author_ID){
        this.title = Title;
        this.date = Date;
        this.time = Time;
        this.description = Description;
        this.numberComment = NumberComment;
        this.author_id = Author_ID;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNumberComment() {
        return numberComment;
    }

    public void setNumberComment(String numberComment) {
        this.numberComment = numberComment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

}

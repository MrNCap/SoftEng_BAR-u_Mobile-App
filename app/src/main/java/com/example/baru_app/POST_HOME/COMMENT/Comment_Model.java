package com.example.baru_app.POST_HOME.COMMENT;

import com.google.firebase.firestore.DocumentId;

public class Comment_Model {
    private String date;
    private String time;
    private String comment;
    private String author_id;
    private String docID;
    private String postID;
    public Comment_Model(){

    }

    @DocumentId
    public String getDocID() {
        return docID;
    }


    public Comment_Model(String Date, String comment, String Time, String Author_ID){
        this.comment = comment;
        this.author_id = Author_ID;
        this.date = Date;
        this.time = Time;

    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }
}

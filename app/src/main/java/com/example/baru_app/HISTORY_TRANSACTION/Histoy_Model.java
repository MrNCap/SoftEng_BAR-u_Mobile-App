package com.example.baru_app.HISTORY_TRANSACTION;

import com.google.firebase.firestore.DocumentId;

public class Histoy_Model {
    private String author_id;
    private String date_complete;
    private String purpose;
    private String status;
    private String type;
    private String date_request;
    private String docID;


    public String getType() {
        return type;
    }

    public String getDate_request() {
        return date_request;
    }

    public void setDate_request(String date_request) {
        this.date_request = date_request;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Histoy_Model(){

    }


    @DocumentId
    public String getDocID() {
        return docID;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getDate_complete() {
        return date_complete;
    }

    public void setDate_complete(String date_complete) {
        this.date_complete = date_complete;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

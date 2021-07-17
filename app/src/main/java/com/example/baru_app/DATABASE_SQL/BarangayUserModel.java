package com.example.baru_app.DATABASE_SQL;

public class BarangayUserModel {
    private int id;
    private String user_id;
    private String barangay;

    public BarangayUserModel(int id, String user_id, String barangay) {
        this.id = id;
        this.user_id = user_id;
        this.barangay = barangay;
    }


    @Override
    public String toString() {
        return "BarangayUserModel{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", barangay=" + barangay +
                '}';
    }


//Setter & Getters


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
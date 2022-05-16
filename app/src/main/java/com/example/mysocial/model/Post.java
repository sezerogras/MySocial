package com.example.mysocial.model;

public class Post {

   public String email;
    public String comment ;
   public String downloadUrl;

    public Post(String comment , String email, String downloadUrl) {
        this.comment = comment;
        this.email = email;
        this.downloadUrl = downloadUrl;

    }
}

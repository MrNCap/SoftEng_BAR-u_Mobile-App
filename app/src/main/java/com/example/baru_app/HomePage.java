package com.example.baru_app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.POST_HOME.OpenPost;
import com.example.baru_app.POST_HOME.Post_Adapter;
import com.example.baru_app.POST_HOME.Post_Model;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomePage extends AppCompatActivity {
    FirebaseFirestore firestoreDB;
    com.example.baru_app.POST_HOME.Post_Adapter Post_Adapter;
    CollectionReference PostListRef;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay;
    StorageReference Address_storageRef;
    FirebaseStorage firebaseStorage;
    CircleImageView userpic_home;
    DatabaseHelper databasehelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        //FIREBASE
        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //WIDGETS
        userpic_home = findViewById(R.id.home_user_profile);

        //DB SQL
        databasehelper = new DatabaseHelper(HomePage.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();
        PostListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post");

        //USER PIC
        Address_storageRef = firebaseStorage.getReference().child("PROFILE/users/" +  userID + "/user_profile");
        Address_storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(userpic_home);
            }
        });



        //PUT POST
        post_recycleView();


        Post_Adapter.setOnItemClickListner(new Post_Adapter.OnItemClicklistener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                String id = documentSnapshot.getId();
                Intent OpenPost = new Intent(HomePage.this, com.example.baru_app.POST_HOME.OpenPost.class);
                OpenPost.putExtra("pass_ID",id);
                startActivity(OpenPost);
            }
        });



    }
    public void GoTo_Services(View view){
        startActivity(new Intent(getApplicationContext(),Services.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void GoTo_Profile(View view){
        startActivity(new Intent(getApplicationContext(),Profile.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

    }
    public void AddPost(View view){
        Toast.makeText(this, "New Post Dialog", Toast.LENGTH_SHORT).show();

    }
    public void post_recycleView(){
        Query query = PostListRef.orderBy("date",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Post_Model> Post_Options = new FirestoreRecyclerOptions.Builder<Post_Model>()
                .setQuery(query, Post_Model.class)
                .build();
        Post_Adapter = new Post_Adapter(Post_Options);
        RecyclerView post_View = findViewById(R.id.home_post_recycleView);
        post_View.setHasFixedSize(true);
        post_View.setLayoutManager(new LinearLayoutManager(this));
        post_View.setAdapter(Post_Adapter);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Post_Adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Post_Adapter.stopListening();
    }
}
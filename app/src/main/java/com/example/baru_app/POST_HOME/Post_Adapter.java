package com.example.baru_app.POST_HOME;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Post_Adapter extends FirestoreRecyclerAdapter<Post_Model, Post_Adapter.PostHolder> {
    FirebaseFirestore firestoreDB;
    DocumentReference PostListRef;
    CollectionReference commentCounter;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay,postID;
    StorageReference Address_storageRef,BarangayProfile;
    FirebaseStorage firebaseStorage;
    DatabaseHelper databasehelper;
    OnItemClicklistener listener;

    public Post_Adapter(@NonNull FirestoreRecyclerOptions<Post_Model> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, int position, @NonNull Post_Model model) {
        postID = model.getDocID();
        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //DB SQL
        databasehelper = new DatabaseHelper(holder.itemView.getContext());
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();


        PostListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(model.getAuthor_id());
        PostListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if(value.getBoolean("admin") == false){
                    holder.Post_Title.setText(value.getString("firstName") +" " + value.getString("lastName") );
                    holder.Post_Author.setText("Brgy. "+value.getString("barangay"));
                    holder.Post_Description.setText(model.getDescription());
                    holder.Post_Date.setText(model.getDate());
                    holder.Post_Time.setText(model.getTime());
                    //USER PIC
                    Address_storageRef = firebaseStorage.getReference().child("PROFILE/users/" +  model.getAuthor_id() + "/user_profile");
                    Address_storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(holder.Post_Profile);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }else{
                    holder.Post_Title.setText(model.getTitle());
                    holder.Post_Author.setText("Brgy. "+value.getString("barangay"));
                    holder.Post_Description.setText(model.getDescription());
                    holder.Post_Date.setText(model.getDate());
                    holder.Post_Time.setText(model.getTime());
                    BarangayProfile = firebaseStorage.getReference().child("BARANGAY LOGO/"+sql_return_barangay+"/profile_brgy.jpg");
                    BarangayProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(holder.Post_Profile);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }

                holder.Post_NumComment.setText(model.getNumberComment());

            }
        });






    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_post_list,parent, false);
        return new PostHolder(view);
    }

    class PostHolder extends RecyclerView.ViewHolder{
        TextView Post_Title,Post_Date,Post_Description,Post_NumComment,Post_Time,Post_Author;
        ImageView Post_Profile;
        String post_id;
        public PostHolder(@NonNull View itemView) {
            super(itemView);
            Post_Author = itemView.findViewById(R.id.postAuthor);
            Post_Title = itemView.findViewById(R.id.postTitle);
            Post_Date = itemView.findViewById(R.id. postDate);
            Post_Time = itemView.findViewById(R.id. postTime);
            Post_Description = itemView.findViewById(R.id. postContent);
            Post_NumComment = itemView.findViewById(R.id. post_commentSize);
            Post_Profile = itemView.findViewById(R.id.home_brgy_profile);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position),position);
                    }
                }
            });

        }


    }
    public interface OnItemClicklistener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);


    }
    public void setOnItemClickListner(OnItemClicklistener listener){
        this.listener = listener;
    }
}

package com.example.baru_app.POST_HOME.COMMENT;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Comment_Adapter extends FirestoreRecyclerAdapter<Comment_Model, Comment_Adapter.PostHolder> {
    FirebaseFirestore firestoreDB;
    DocumentReference PostListRef;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay;
    StorageReference Address_storageRef,BarangayProfile;
    FirebaseStorage firebaseStorage;
    DatabaseHelper databasehelper;
    OnLongClickListener listener_long;
    Dialog openCommentSetting,editComment,deleteComment;
    Button edit_comment_setting,delete_comment_setting,update_comment,delete_comment,cancel_delete_comment;
    TextView editAuthor,editDate,editTime;
    EditText editComment_user;
    String comment_id,post_id;
    DocumentReference CommentListRef;

    public Comment_Adapter(@NonNull FirestoreRecyclerOptions<Comment_Model> options) {
        super(options);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }




    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, int position, @NonNull Comment_Model model) {
        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //DB SQL
        databasehelper = new DatabaseHelper(holder.itemView.getContext());
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();

        post_id = model.getPostID();
        comment_id = model.getDocID();

        //Comment User Setting
//        openCommentSetting = new Dialog(holder.itemView.getContext());
//        openCommentSetting.setContentView(R.layout.comment_settings_popout);
//        openCommentSetting.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        edit_comment_setting = openCommentSetting.findViewById(R.id.edit_comment_setting);
//        delete_comment_setting = openCommentSetting.findViewById(R.id.delete_comment_setting);
//            //DeleteComment User dialog
//            deleteComment = new Dialog(holder.itemView.getContext());
//            deleteComment.setContentView(R.layout.delete_comment_dialog);
//            deleteComment.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                //widgets
//                delete_comment = deleteComment.findViewById(R.id.accept_delete_btn);
//                cancel_delete_comment = deleteComment.findViewById(R.id.decline_delete_btn);
//            //Comment User dialog
//            editComment = new Dialog(holder.itemView.getContext());
//            editComment.setContentView(R.layout.edit_comment);
//            editComment.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                //widgets
//                editAuthor  = editComment.findViewById(R.id.edit_author);
//                editDate  = editComment.findViewById(R.id.edit_date);
//                editTime  = editComment.findViewById(R.id.edit_time);
//                editComment_user = editComment.findViewById(R.id.commentBox_edit);
//                update_comment = editComment.findViewById(R.id.update_comment_btn);



        PostListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(model.getAuthor_id());
        PostListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if(value.getBoolean("admin") == false){
                    holder.Comment_Author.setText(value.getString("firstName") +" " + value.getString("lastName"));
                    holder.Comment_Content.setText(model.getComment());
                    holder.Comment_Date.setText(model.getDate());
                    holder.Comment_Time.setText(model.getTime());
                    //USER PIC
                    Address_storageRef = firebaseStorage.getReference().child("PROFILE/users/" +  userID + "/user_profile");
                    Address_storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(holder.Comment_Profile);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }else{
                    holder.Comment_Author.setText("Brgy. "+value.getString("barangay"));
                    holder.Comment_Content.setText(model.getComment());
                    holder.Comment_Date.setText(model.getDate());
                    holder.Comment_Time.setText(model.getTime());
                    BarangayProfile = firebaseStorage.getReference().child("BARANGAY LOGO/"+sql_return_barangay+"/profile_brgy.jpg");
                    BarangayProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(holder.Comment_Profile);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }



            }
        });



    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list,parent, false);
        return new PostHolder(view);
    }

    class PostHolder extends RecyclerView.ViewHolder{
        TextView Comment_Author,Comment_Date,Comment_Content,Comment_Time;
        ImageView Comment_Profile;
        public PostHolder(@NonNull View itemView) {
            super(itemView);
            Comment_Author = itemView.findViewById(R.id.tvCommentAuthor);
            Comment_Date = itemView.findViewById(R.id. tvCommentDate);
            Comment_Time = itemView.findViewById(R.id. tvCommentTime);
            Comment_Content = itemView.findViewById(R.id. tvCommentContent);
            Comment_Profile = itemView.findViewById(R.id. profileImage);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener_long != null){
                        listener_long.onItemClick(getSnapshots().getSnapshot(position),position);
                    }
                    return false;
                }
            });

        }

    }
    public interface OnLongClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }
    public void setOnLongClickListener(OnLongClickListener listener_long){
        this.listener_long = listener_long;
    }
}

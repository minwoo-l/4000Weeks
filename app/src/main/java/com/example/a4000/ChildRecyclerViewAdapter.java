//package com.example.a4000;
//
//import android.content.Intent;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide; // <- IMPORTANT: Glide import
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot; // <- IMPORTANT: Firebase imports
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.List;
//
//public class ChildRecyclerViewAdapter extends RecyclerView.Adapter<ChildRecyclerViewAdapter.ChildViewHolder> {
//
//    private List<ChildItem> childList;
//    private String parentTitle;
//
//    // Firebase instances
//    private FirebaseAuth mAuth;
//    private FirebaseUser user;
//
//    public ChildRecyclerViewAdapter(List<ChildItem> childList, String parentTitle) {
//        this.childList = childList;
//        this.parentTitle = parentTitle;
//    }
//
//    public static class ChildViewHolder extends RecyclerView.ViewHolder {
//        ImageView imageView;
//        TextView title;
//
//        public ChildViewHolder(View itemView) {
//            super(itemView);
//            imageView = itemView.findViewById(R.id.childLogoIv);
//            title = itemView.findViewById(R.id.childTitleTv);
//        }
//    }
//
//    @NonNull
//    @Override
//    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_item, parent, false);
//        return new ChildViewHolder(view);
//    }
//
//    // Replace the existing onBindViewHolder with this one
//    @Override
//    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
//        ChildItem currentItem = childList.get(position);
//        holder.title.setText(currentItem.getTitle());
//
//        // --- Start of Image Loading Logic ---
//
//        // 1. Set the default placeholder image initially
//        holder.imageView.setImageResource(R.drawable.ic_action_name);
//
//        // 2. Get the database path for this specific week
//        String ym = parentTitle;
//        String[] date = ym.split("\\.");
//        String year = date[0];
//        String month = date[1];
//        String week = currentItem.getTitle();
//
//        mAuth = FirebaseAuth.getInstance();
//        user = mAuth.getCurrentUser();
//
//        if (user == null) {
//            return;
//        }
//        String uid = user.getUid();
//
//        DatabaseReference imageInfoRef = FirebaseDatabase.getInstance().getReference()
//                .child("users").child(uid).child("Image")
//                .child(year).child(month).child(week);
//
//        // DEBUG STEP 1: Log the path we are checking in the database.
//        Log.d("ImageDebug", "Checking path: " + imageInfoRef.toString());
//
//        // 3. Listen for the data at that path
//        imageInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // DEBUG STEP 2: Check if the path exists at all.
//                if (snapshot.exists()) {
//                    Log.d("ImageDebug", "Path exists for " + week);
//                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//                        Model model = childSnapshot.getValue(Model.class);
//                        if (model != null && model.getImageUrl() != null) {
//                            // DEBUG STEP 3: Log the URL we found.
//                            Log.d("ImageDebug", "Image URL found: " + model.getImageUrl());
//
//                            // 4. If a valid URL is found, use Glide to load the image
//                            Glide.with(holder.itemView.getContext())
//                                    .load(model.getImageUrl())
//                                    .placeholder(R.drawable.ic_action_name)
//                                    .into(holder.imageView);
//                            return; // We found the image, no need to continue loop
//                        } else {
//                            Log.d("ImageDebug", "Model or URL is null for " + week);
//                        }
//                    }
//                } else {
//                    // DEBUG STEP 4: This will tell us if the path is wrong.
//                    Log.d("ImageDebug", "Path does NOT exist for " + week);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("ImageDebug", "Failed to read image URL for " + week, error.toException());
//            }
//        });
//
//        // --- End of Image Loading Logic ---
//
//        holder.itemView.setOnClickListener(view -> {
//            Intent intent = new Intent(view.getContext(), ImageAdd.class);
//            intent.putExtra("WEEK", currentItem.getTitle());
//            intent.putExtra("YM", parentTitle);
//            view.getContext().startActivity(intent);
//        });
//    }
//
////    // There should ONLY be ONE onBindViewHolder method
////    @Override
////    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
////        ChildItem currentItem = childList.get(position);
////        holder.title.setText(currentItem.getTitle());
////
////        // --- Start of Image Loading Logic ---
////
////        // 1. Set the default placeholder image initially
////        holder.imageView.setImageResource(R.drawable.ic_action_name);
////
////        // 2. Get the database path for this specific week
////        String ym = parentTitle;
////        String[] date = ym.split("\\.");
////        String year = date[0];
////        String month = date[1];
////        String week = currentItem.getTitle();
////
////        mAuth = FirebaseAuth.getInstance();
////        user = mAuth.getCurrentUser();
////
////        // Check if user is null to prevent crashes
////        if (user == null) {
////            return; // Don't try to load data if user isn't logged in
////        }
////        String uid = user.getUid(); // Use UID for security, as we set up in the rules
////
////        // This is the path to where the image URL is stored in the Realtime Database
////        DatabaseReference imageInfoRef = FirebaseDatabase.getInstance().getReference()
////                .child("users").child(uid).child("Image")
////                .child(year).child(month).child(week);
////
////        // 3. Listen for the data at that path
////        imageInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
////            @Override
////            public void onDataChange(@NonNull DataSnapshot snapshot) {
////                if (snapshot.exists()) {
////                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
////                        Model model = childSnapshot.getValue(Model.class);
////                        if (model != null && model.getImageUrl() != null) {
////                            // 4. If a valid URL is found, use Glide to load the image
////                            Glide.with(holder.itemView.getContext())
////                                    .load(model.getImageUrl())
////                                    .placeholder(R.drawable.ic_action_name) // Show placeholder while loading
////                                    .into(holder.imageView);
////                            break; // Exit after finding the first image
////                        }
////                    }
////                }
////            }
////
////            @Override
////            public void onCancelled(@NonNull DatabaseError error) {
////                Log.e("Firebase", "Failed to read image URL.", error.toException());
////            }
////        });
////
////        // --- End of Image Loading Logic ---
////
////        holder.itemView.setOnClickListener(view -> {
////            Intent intent = new Intent(view.getContext(), ImageAdd.class);
////            intent.putExtra("WEEK", currentItem.getTitle());
////            intent.putExtra("YM", parentTitle);
////            view.getContext().startActivity(intent);
////        });
////    }
//
//    @Override
//    public int getItemCount() {
//        return childList.size();
//    }
//}
//
//
////package com.example.a4000;
////
////import android.content.Intent;
////import android.graphics.Bitmap;
////import android.graphics.BitmapFactory;
////import android.util.Log;
////import android.view.LayoutInflater;
////import android.view.View;
////import android.view.ViewGroup;
////import android.widget.ImageView;
////import android.widget.TextView;
////
////import androidx.annotation.NonNull;
////import androidx.recyclerview.widget.RecyclerView;
////
////import com.google.android.gms.tasks.OnFailureListener;
////import com.google.android.gms.tasks.OnSuccessListener;
////import com.google.firebase.auth.FirebaseAuth;
////import com.google.firebase.auth.FirebaseUser;
////import com.google.firebase.storage.FileDownloadTask;
////import com.google.firebase.storage.FirebaseStorage;
////import com.google.firebase.storage.StorageReference;
////
////import java.io.File;
////import java.io.IOException;
////import java.util.List;
////
////public class ChildRecyclerViewAdapter extends RecyclerView.Adapter<ChildRecyclerViewAdapter.ChildViewHolder> {
////    private List<ChildItem> childList;
////
////    //
////    FirebaseAuth mAuth;
////
////    FirebaseUser user;
////
////    private String userName;
////
////    //
////
////    private String parentTitle;
////
////    public ChildRecyclerViewAdapter(List<ChildItem> childList) {this.childList = childList;}
////
////    public ChildRecyclerViewAdapter(List<ChildItem> childList, String parentTitle) {
////        this.childList = childList;
////        this.parentTitle = parentTitle;
////    }
////
////
////
////    public class ChildViewHolder extends RecyclerView.ViewHolder {
////        ImageView imageView;
////        TextView title;
////
////        public ChildViewHolder(View itemView) {
////            super(itemView);
////            imageView = itemView.findViewById(R.id.childLogoIv);
////            title = itemView.findViewById(R.id.childTitleTv);
////        }
////    }
////
////    @Override
////    public ChildViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
////        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_item, parent, false);
////        return new ChildViewHolder(view);
////    }
////
////    @Override
////    public void onBindViewHolder(ChildViewHolder holder, int position) {
////        holder.imageView.setImageResource(childList.get(position).getImage());
////        holder.title.setText(childList.get(position).getTitle());
////        holder.itemView.setOnClickListener(new View.OnClickListener()
////        {
////
////            @Override
////            public void onClick(View view)
////            {
////                Intent intent = new Intent(view.getContext(), ImageAdd.class);
////                intent.putExtra("WEEK", childList.get(position).getTitle());
////                intent.putExtra("YM",parentTitle);
////                //
////                view.getContext().startActivity(intent);
////            }
////        });
////
////        //
////        String ym = parentTitle;
////        String[] date = ym.split("\\.");
////        String year = date[0];
////        Log.d("child", "year" + year);
////        String month = date[1];
////        Log.d("child", "month" + month);
////        String week = childList.get(position).getTitle();
////
////        FirebaseStorage storage = FirebaseStorage.getInstance();
////
////        mAuth = FirebaseAuth.getInstance();
////        user = mAuth.getCurrentUser();
////
////        userName = user.getDisplayName();
////
////        File localFile;
////
////        StorageReference imageRef = storage.getReference().child(userName).child(year).child(month).child(week+"."+"jpg");
////        try {
////            localFile = File.createTempFile(userName+year+month+week,"jpg");
////        } catch (IOException e) {
////            throw new RuntimeException(e);
////        }
////        imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
////        {
////
////            @Override
////            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
////            {
////                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
////                holder.imageView.setImageBitmap(bitmap);
////            }
////
////        }).addOnFailureListener(new OnFailureListener() {
////            @Override
////            public void onFailure(@NonNull Exception e) {
////                // Handle any errors
////            }
////        });
////
////
////
////        //
////    }
////
////    @Override
////    public int getItemCount() {
////        return childList.size();
////    }
////}
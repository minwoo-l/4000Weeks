package com.example.a4000;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UnifiedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View type constants
    private static final int TYPE_GRANDPARENT = 0;
    private static final int TYPE_PARENT = 1;
    private static final int TYPE_CHILD = 2;

    private List<DisplayableItem> itemList;

    public UnifiedAdapter(List<DisplayableItem> itemList) {
        this.itemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof GrandParentItem) {
            return TYPE_GRANDPARENT;
        } else if (itemList.get(position) instanceof ParentItem) {
            return TYPE_PARENT;
        } else {
            return TYPE_CHILD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_GRANDPARENT:
                View gpView = inflater.inflate(R.layout.grandparent_item, parent, false);
                return new GrandParentViewHolder(gpView);
            case TYPE_PARENT:
                View pView = inflater.inflate(R.layout.parent_item, parent, false);
                return new ParentViewHolder(pView);
            case TYPE_CHILD:
                View cView = inflater.inflate(R.layout.child_item, parent, false);
                return new ChildViewHolder(cView);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Use 'final' for the position to use it inside the listener
        final int adapterPosition = holder.getAdapterPosition();

        switch (holder.getItemViewType()) {
            case TYPE_GRANDPARENT:
                GrandParentViewHolder gpvh = (GrandParentViewHolder) holder;
                GrandParentItem gpItem = (GrandParentItem) itemList.get(adapterPosition);
                gpvh.title.setText(gpItem.getTitle());
                gpvh.image.setImageResource(gpItem.getGrandparentImage());

                // Click listener to expand/collapse the list of months
                // THIS IS THE NEW, CORRECTED CODE BLOCK
                gpvh.itemView.setOnClickListener(v -> {
                    // Toggle the expandable state
                    gpItem.setExpandable(!gpItem.isExpandable());

                    if (gpItem.isExpandable()) {
                        // EXPAND LOGIC: This part is simple. Add the months after the year.
                        itemList.addAll(adapterPosition + 1, gpItem.getParentItemList());
                    } else {
                        // COLLAPSE LOGIC: This is where we add the new, smart logic.
                        // We need to remove the months AND any of their expanded weeks.

                        List<DisplayableItem> itemsToRemove = new ArrayList<>();

                        // Loop through the months belonging to this year
                        for (ParentItem parent : gpItem.getParentItemList()) {
                            // If a month was itself expanded, add its weeks to our removal list first
                            if (parent.isExpandable()) {
                                itemsToRemove.addAll(parent.getChildItemList());
                                // Important: Also reset the month's state so it's collapsed next time
                                parent.setExpandable(false);
                            }
                        }

                        // Now, add the months themselves to the removal list
                        itemsToRemove.addAll(gpItem.getParentItemList());

                        // Remove everything (all weeks and months) from the main list
                        itemList.removeAll(itemsToRemove);
                    }

                    // Refresh the entire list to show the changes
                    notifyDataSetChanged();
                });
                break;

            case TYPE_PARENT:
                ParentViewHolder pvh = (ParentViewHolder) holder;
                ParentItem pItem = (ParentItem) itemList.get(adapterPosition);
                pvh.title.setText(pItem.getTitle());
                pvh.image.setImageResource(pItem.getImage());

                // Click listener to expand/collapse the list of weeks
                pvh.itemView.setOnClickListener(v -> {
                    pItem.setExpandable(!pItem.isExpandable());
                    if (pItem.isExpandable()) {
                        itemList.addAll(adapterPosition + 1, pItem.getChildItemList());
                    } else {
                        itemList.removeAll(pItem.getChildItemList());
                    }
                    notifyDataSetChanged();
                });
                break;

            case TYPE_CHILD:
                ChildViewHolder cvh = (ChildViewHolder) holder;
                ChildItem cItem = (ChildItem) itemList.get(adapterPosition);
                cvh.title.setText(cItem.getTitle());
                loadImageForChild(cvh, cItem, adapterPosition); // Call helper method to load image

                // Click listener to go to the ImageAdd activity
                cvh.itemView.setOnClickListener(view -> {
                    String parentTitle = findParentTitle(adapterPosition);
                    Intent intent = new Intent(view.getContext(), ImageAdd.class);
                    intent.putExtra("WEEK", cItem.getTitle());
                    intent.putExtra("YM", parentTitle);
                    view.getContext().startActivity(intent);
                });
                break;
        }
    }

    private void loadImageForChild(ChildViewHolder holder, ChildItem item, int position) {
        holder.image.setImageResource(R.drawable.ic_action_name); // Set placeholder

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String parentTitle = findParentTitle(position);
        if (parentTitle.isEmpty()) return;

        String[] date = parentTitle.split("\\.");
        String year = date[0];
        String month = date[1];
        String week = item.getTitle();

        DatabaseReference imageInfoRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("Image")
                .child(year).child(month).child(week);

        imageInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        Model model = childSnapshot.getValue(Model.class);
                        if (model != null && model.getImageUrl() != null) {
                            Glide.with(holder.itemView.getContext())
                                    .load(model.getImageUrl())
                                    .placeholder(R.drawable.ic_action_name)
                                    .into(holder.image);
                            return;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UnifiedAdapter", "Failed to load image URL", error.toException());
            }
        });
    }

    private String findParentTitle(int childPosition) {
        for (int i = childPosition - 1; i >= 0; i--) {
            if (itemList.get(i) instanceof ParentItem) {
                return ((ParentItem) itemList.get(i)).getTitle();
            }
        }
        return ""; // Should not happen if data is structured correctly
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // --- ViewHolders for each type ---
    static class GrandParentViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        public GrandParentViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.grandParentTitleTv);
            image = itemView.findViewById(R.id.grandParentLogoIv);
        }
    }

    static class ParentViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        public ParentViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.parentTitleTv);
            image = itemView.findViewById(R.id.parentLogoIv);
        }
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.childTitleTv);
            image = itemView.findViewById(R.id.childLogoIv);
        }
    }
}
//package com.example.a4000;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//public class GrandParentRecyclerViewAdapter extends RecyclerView.Adapter<GrandParentRecyclerViewAdapter.GrandParentRecyclerViewHolder>
//{
//    private final List<GrandParentItem> grandParentItemList;
//
//    public GrandParentRecyclerViewAdapter(List<GrandParentItem> grandParentItemList) {
//        this.grandParentItemList = grandParentItemList;
//    }
//
//    public static class GrandParentRecyclerViewHolder extends  RecyclerView.ViewHolder
//    {
//
//        public ImageView grandParentImageView;
//        public TextView grandParentTitle;
//        public RecyclerView parentRecyclerView;
//
//        public ConstraintLayout grandConstraintLayout;
//
//        public GrandParentRecyclerViewHolder(@NonNull View itemView)
//        {
//            super(itemView);
//            grandParentImageView = itemView.findViewById(R.id.grandParentLogoIv);
//            grandParentTitle = itemView.findViewById(R.id.grandParentTitleTv);
//            parentRecyclerView = itemView.findViewById(R.id.parentRecyclerView);
//            grandConstraintLayout = itemView.findViewById(R.id.grandConstraintLayout);
//        }
//    }
//
//    @NonNull
//    @Override
//    public GrandParentRecyclerViewAdapter.GrandParentRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grandparent_item,parent,false);
//        return new GrandParentRecyclerViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull GrandParentRecyclerViewAdapter.GrandParentRecyclerViewHolder holder, int position) {
//        GrandParentItem grandparentItem = grandParentItemList.get(position);
//
//        holder.grandParentTitle.setText(grandparentItem.getTitle());
//        holder.grandParentImageView.setImageResource(grandparentItem.getGrandparentImage());
//
//        List<ParentItem> limitedParentItemList = grandparentItem.getParentItemList().subList(0, Math.min(grandparentItem.getParentItemList().size(), 12));
//
//        ParentRecyclerViewAdapter adapter = new ParentRecyclerViewAdapter(limitedParentItemList);
//        holder.parentRecyclerView.setAdapter(adapter);
//
//        holder.parentRecyclerView.setHasFixedSize(true);
//        holder.parentRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
//
//        boolean isExpandable = grandparentItem.isExpandable();
//        holder.parentRecyclerView.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
//
//        holder.grandConstraintLayout.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                isAnyItemExpanded(position);
//                grandparentItem.setExpandable(!grandparentItem.isExpandable());
//                notifyItemChanged(position);
//            }
//        });
//
//
//
//    }
//
//    private void isAnyItemExpanded(int position) {
//        int temp = -1;
//        for (int i = 0; i < grandParentItemList.size(); i++) {
//            if (grandParentItemList.get(i).isExpandable() && i != position) {
//                grandParentItemList.get(i).setExpandable(false);
//                temp = i;
//                break;
//            }
//        }
//        if (temp >= 0) {
//            notifyItemChanged(temp);
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return grandParentItemList.size();
//    }
//}

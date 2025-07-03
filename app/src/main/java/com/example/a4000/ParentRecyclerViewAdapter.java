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
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//public class ParentRecyclerViewAdapter extends RecyclerView.Adapter<ParentRecyclerViewAdapter.ParentRecyclerViewHolder> {
//    private final List<ParentItem> parentItemList;
//
//    public ParentRecyclerViewAdapter(List<ParentItem> parentItemList) {
//        this.parentItemList = parentItemList;
//    }
//
//    public static class ParentRecyclerViewHolder extends RecyclerView.ViewHolder {
//        public ImageView parentImageView;
//        public TextView parentTitle;
//        public RecyclerView childRecyclerView;
//        public ConstraintLayout constraintLayout;
//
//        public ParentRecyclerViewHolder(@NonNull View itemView) {
//            super(itemView);
//            parentImageView = itemView.findViewById(R.id.parentLogoIv);
//            parentTitle = itemView.findViewById(R.id.parentTitleTv);
//            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
//            constraintLayout = itemView.findViewById(R.id.constraintLayout);
//        }
//    }
//
//    @NonNull
//    @Override
//    public ParentRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
//    {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parent_item, parent, false);
//        return new ParentRecyclerViewHolder(view);
//    }
//
//
//    @Override
//    public void onBindViewHolder(@NonNull ParentRecyclerViewHolder holder, int position)
//    {
//
//        ParentItem parentItem = parentItemList.get(position);
//
//        holder.parentTitle.setText(parentItem.getTitle());
//        holder.parentImageView.setImageResource(parentItem.getImage());
//
////        holder.childRecyclerView.setHasFixedSize(true);
//        holder.childRecyclerView.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 4));
//
//        ChildRecyclerViewAdapter adapter = new ChildRecyclerViewAdapter(parentItem.getChildItemList(),parentItem.getTitle());
//        holder.childRecyclerView.setAdapter(adapter);
//
//        //expandable functionality
//        boolean isExpandable = parentItem.isExpandable();
//        holder.childRecyclerView.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
//
//        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isAnyItemExpanded(position);
//                parentItem.setExpandable(!parentItem.isExpandable());
//                notifyItemChanged(position);
//            }
//        });
//    }
//
//    private void isAnyItemExpanded(int position) {
//        int temp = -1;
//        for (int i = 0; i < parentItemList.size(); i++) {
//            if (parentItemList.get(i).isExpandable() && i != position) {
//                parentItemList.get(i).setExpandable(false);
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
//        return parentItemList.size();
//    }
//}

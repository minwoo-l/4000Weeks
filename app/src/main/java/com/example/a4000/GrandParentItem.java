package com.example.a4000;

import java.util.*;

public class GrandParentItem implements DisplayableItem {
    public String title;
    public ArrayList<ParentItem> parentItemList;
    public int grandparentImage;
    public boolean isExpandable;

    public GrandParentItem(String title, ArrayList<ParentItem> parentItemList, int grandparentImage)
    {
        this.title = title;
        this.parentItemList = parentItemList;
        this.grandparentImage = grandparentImage;
        this.isExpandable = false;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<ParentItem> getParentItemList() {
        return parentItemList;
    }

    public int getGrandparentImage() {
        return grandparentImage;
    }

    public boolean isExpandable() {
        return isExpandable;
    }
}

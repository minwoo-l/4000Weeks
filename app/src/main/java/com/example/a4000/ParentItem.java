package com.example.a4000;

import java.util.ArrayList;

public class ParentItem implements DisplayableItem
{
    public String title;
    public int image;
    public ArrayList<ChildItem> childItemList;
    public boolean isExpandable;

    public ParentItem(String title, int image, ArrayList<ChildItem> childItemList)
    {
        this.title = title;
        this.image = image;
        this.childItemList = childItemList;
        this.isExpandable = false;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }

    public ArrayList<ChildItem> getChildItemList() {
        return childItemList;
    }

    public boolean isExpandable()
    {
        return isExpandable;
    }
}

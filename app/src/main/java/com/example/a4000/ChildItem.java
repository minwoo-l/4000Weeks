package com.example.a4000;

public class ChildItem implements DisplayableItem
{
    public String title;
    public int image;

    public ChildItem(String title, int image) {
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }
}

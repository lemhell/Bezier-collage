package com.lemhell.beziercollage.recycler;

public class RecyclerItem {
    private int imageId, size;

    public RecyclerItem(int imageId, int size) {
        this.imageId = imageId;
        this.size = size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getImageId() {
        return this.imageId;
    }

    public int getSize() {
        return this.size;
    }
}

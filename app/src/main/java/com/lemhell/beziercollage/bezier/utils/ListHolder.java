package com.lemhell.beziercollage.bezier.utils;

import java.util.ArrayList;

public class ListHolder {
    public ArrayList<Float> x, y;

    public ListHolder(ArrayList<Float> x, ArrayList<Float> y) {
        this.x = x;
        this.y = y;
    }

    public int size() {
        return x.size();
    }
}

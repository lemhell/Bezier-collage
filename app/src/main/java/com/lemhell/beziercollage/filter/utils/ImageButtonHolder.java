package com.lemhell.beziercollage.filter.utils;

import android.graphics.Bitmap;
import android.widget.ImageButton;

public class ImageButtonHolder {
    public ImageButton imageButton;
    public Bitmap fullImage;
    public Bitmap scaledImage;

    public ImageButtonHolder(ImageButton imageButton, Bitmap fullImage, Bitmap scaledImage) {
        this.imageButton = imageButton;
        this.fullImage = fullImage;
        this.scaledImage = scaledImage;
    }
}

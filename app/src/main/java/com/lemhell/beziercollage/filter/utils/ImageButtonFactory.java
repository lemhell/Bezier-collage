package com.lemhell.beziercollage.filter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageButton;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

public class ImageButtonFactory {
    Context context = null;
    GPUImage currentImage = null;
    public ImageButtonFactory(Context context) {
        this.context = context;
    }

    public ImageButton createButton(Bitmap rawPic, GPUImageFilter filter, View.OnClickListener listener) {
        ImageButton button = createButtonWithoutPic();
        if (filter != null) {
            Bitmap filteredPic = getBitmap(rawPic, filter);
            button.setImageBitmap(filteredPic);
        } else {
            button.setImageBitmap(rawPic);
        }
        button.setOnClickListener(listener);
        button.setBackgroundDrawable(null);
        return button;
    }

    private Bitmap getBitmap(Bitmap rawPic, GPUImageFilter filter) {
        GPUImage tempImage = new GPUImage(context);
        tempImage.setImage(rawPic);
        tempImage.setFilter(filter);
        tempImage.requestRender();
        currentImage = tempImage;
        return tempImage.getBitmapWithFilterApplied();
    }

    private ImageButton createButtonWithoutPic() {
        return new ImageButton(context);
    }
}

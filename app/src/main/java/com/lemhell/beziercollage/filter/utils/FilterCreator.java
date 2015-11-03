package com.lemhell.beziercollage.filter.utils;

import android.content.Context;

import com.lemhell.beziercollage.filter.filters.IF1977Filter;
import com.lemhell.beziercollage.filter.filters.IFAmaroFilter;
import com.lemhell.beziercollage.filter.filters.IFBrannanFilter;
import com.lemhell.beziercollage.filter.filters.IFEarlybirdFilter;
import com.lemhell.beziercollage.filter.filters.IFHefeFilter;
import com.lemhell.beziercollage.filter.filters.IFHudsonFilter;
import com.lemhell.beziercollage.filter.filters.IFInkwellFilter;
import com.lemhell.beziercollage.filter.filters.IFLomofiFilter;
import com.lemhell.beziercollage.filter.filters.IFLordKelvinFilter;
import com.lemhell.beziercollage.filter.filters.IFNashvilleFilter;
import com.lemhell.beziercollage.filter.filters.IFRiseFilter;
import com.lemhell.beziercollage.filter.filters.IFSierraFilter;
import com.lemhell.beziercollage.filter.filters.IFSutroFilter;
import com.lemhell.beziercollage.filter.filters.IFToasterFilter;
import com.lemhell.beziercollage.filter.filters.IFValenciaFilter;
import com.lemhell.beziercollage.filter.filters.IFWaldenFilter;
import com.lemhell.beziercollage.filter.filters.IFXproIIFilter;
import com.lemhell.beziercollage.filter.filters.MyFilter1;

import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageCGAColorspaceFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageDilationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageNonMaximumSuppressionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

public class FilterCreator {
    Context context;
    public final int NUMBER_OF_FILTERS = 26;

    public FilterCreator(Context context) {
        this.context = context;
    }

    public GPUImageFilter getFilterByID(int id) {
        switch(id) {
            case 0:
                return new GPUImageSepiaFilter();
            case 1:
                return new GPUImageColorInvertFilter();
            case 2:
                return new GPUImageBrightnessFilter(-0.3f);
            case 3:
                return new GPUImageEmbossFilter(0.3f);
            case 4:
                return new GPUImageDilationFilter(30);
            case 5:
                return new GPUImageCGAColorspaceFilter();
            case 6:
                return new GPUImageExposureFilter(0.3f);
            case 7:
                return new GPUImageNonMaximumSuppressionFilter();
            case 8:
                return new IF1977Filter(context);
            case 9:
                return new IFAmaroFilter(context);
            case 10:
                return new IFBrannanFilter(context);
            case 11:
                return new IFEarlybirdFilter(context);
            case 12:
                return new IFHefeFilter(context);
            case 13:
                return new IFHudsonFilter(context);
            case 14:
                return new IFInkwellFilter(context);
            case 15:
                return new IFLomofiFilter(context);
            case 16:
                return new IFLordKelvinFilter(context);
            case 17:
                return new IFNashvilleFilter(context);
            case 18:
                return new IFRiseFilter(context);
            case 19:
                return new IFSierraFilter(context);
            case 20:
                return new IFSutroFilter(context);
            case 21:
                return new IFToasterFilter(context);
            case 22:
                return new IFValenciaFilter(context);
            case 23:
                return new IFWaldenFilter(context);
            case 24:
                return new IFXproIIFilter(context);
            case 25:
                return new MyFilter1();
            default:
                return new GPUImageMonochromeFilter();
        }
    }
}


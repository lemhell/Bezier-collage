package com.lemhell.beziercollage.filter.filters;

import android.content.Context;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

public class FilterHelper extends GPUImageFilter {

    private FilterHelper() {}

    private static final int FILTER_NUM = 18;
    private static InstaFilter[] filters;

    public static InstaFilter getFilter(Context context, int index) {
        if (filters == null) {
            filters = new InstaFilter[FILTER_NUM];
        }
        try {
            switch (index){
                case 0:
                    filters[index] = new IFNormalFilter(context);
                    break;
                case 1:
                    filters[index] = new IFAmaroFilter(context);
                    break;
                case 2:
                    filters[index] = new IFRiseFilter(context);
                    break;
                case 3:
                    filters[index] = new IFHudsonFilter(context);
                    break;
                case 4:
                    filters[index] = new IFXproIIFilter(context);
                    break;
                case 5:
                    filters[index] = new IFSierraFilter(context);
                    break;
                case 6:
                    filters[index] = new IFLomofiFilter(context);
                    break;
                case 7:
                    filters[index] = new IFEarlybirdFilter(context);
                    break;
                case 8:
                    filters[index] = new IFSutroFilter(context);
                    break;
                case 9:
                    filters[index] = new IFToasterFilter(context);
                    break;
                case 10:
                    filters[index] = new IFBrannanFilter(context);
                    break;
                case 11:
                    filters[index] = new IFInkwellFilter(context);
                    break;
                case 12:
                    filters[index] = new IFWaldenFilter(context);
                    break;
                case 13:
                    filters[index] = new IFHefeFilter(context);
                    break;
                case 14:
                    filters[index] = new IFValenciaFilter(context);
                    break;
                case 15:
                    filters[index] = new IFNashvilleFilter(context);
                    break;
                case 16:
                    filters[index] = new IF1977Filter(context);
                    break;
                case 17:
                    filters[index] = new IFLordKelvinFilter(context);
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return filters[index];
    }

    public static void destroyFilters() {
        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                try {
                    if (filters[i] != null) {
                        filters[i].destroy();
                        filters[i] = null;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
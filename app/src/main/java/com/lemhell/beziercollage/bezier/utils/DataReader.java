package com.lemhell.beziercollage.bezier.utils;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DataReader {
    private BufferedReader br;
    private AssetManager assetManager;
    public ArrayList<ListHolder> holderList;

    public DataReader(AssetManager assetManager, String filename) {
        this.assetManager = assetManager;
        read(filename);
    }

    private void read(String filename) {
        boolean flag = false;
        holderList = new ArrayList<>();
        try {
            br = new BufferedReader(new InputStreamReader(assetManager.open(filename)));
            ArrayList<Float> xList;
            ArrayList<Float> yList;
            int scale = Integer.parseInt(br.readLine());
            String line = br.readLine();
            while (line != null) {
                xList = new ArrayList<>();
                yList = new ArrayList<>();
                while (!flag) {
                    line = br.readLine();
                    if (line.substring(0, 1).equals("#")) {
                        flag = true;
                    } else {
                        String[] lines = line.split(" ");
                        xList.add(Float.parseFloat(lines[0])*2*scale);
                        yList.add(Float.parseFloat(lines[1])*2*scale);
                    }
                }
                holderList.add(new ListHolder(xList, yList));
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

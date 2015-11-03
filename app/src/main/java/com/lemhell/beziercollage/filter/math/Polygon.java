package com.lemhell.beziercollage.filter.math;


import com.lemhell.beziercollage.bezier.BezierPoint;

import java.util.ArrayList;

public class Polygon {
    // Polygon coordinates.
    private final int[] polyY, polyX;
    // Number of sides in the polygon.
    private final int polySides;
    /**
     * Default constructor.
     * @param px Polygon y coords.
     * @param py Polygon x coords.
     * @param ps Polygon sides count.
     */

    public Polygon( final int[] px, final int[] py, final int ps ) {
        polyX = px;
        polyY = py;
        polySides = ps;
    }

    public Polygon(ArrayList<BezierPoint> points) {
        polyX = new int[points.size()];
        polyY = new int[points.size()];
        int i = 0;
        for (BezierPoint point : points) {
            polyX[i] = (int)point.x;
            polyY[i] = (int)point.y;
            i++;
        }
        polySides = points.size();
    }

    public void setPoly(int index, int valueX, int valueY) {
        this.polyX[index] = valueX;
        this.polyY[index] = valueY;
    }

    /**
     * Checks if the Polygon contains a point.
     * @see "http://alienryderflex.com/polygon/"
     * @param x Point horizontal pos.
     * @param y Point vertical pos.
     * @return Point is in Poly flag.
     */
    public boolean contains( final float x, final float y ) {
        boolean oddTransitions = false;
        for( int i = 0, j = polySides -1; i < polySides; j = i++ ) {
            if( ( polyY[ i ] < y && polyY[ j ] >= y ) || ( polyY[ j ] < y && polyY[ i ] >= y ) ) {
                if( polyX[ i ] + ( y - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < x ) {
                    oddTransitions = !oddTransitions;
                }
            }
        }
        return oddTransitions;
    }
}
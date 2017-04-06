package com.dimension4.dcm2stl.model;

/**
 *
 * @author Brecht
 */

public class CropRegion3D {

    //topRegion
    //a --- b
    //|     |
    //c --- d
    //bottomRegion
    //e --- f
    //|     |
    //g --- h
    
    //topRegion
    public double ax;
    public double ay;
    public double az;
    
    public double bx;
    public double by;
    public double bz;
    
    public double cx;
    public double cy;
    public double cz;
    
    public double dx;
    public double dy;
    public double dz;
    
    //bottomRegion
    public double ex;
    public double ey;
    public double ez;
    
    public double fx;
    public double fy;
    public double fz;
    
    public double gx;
    public double gy;
    public double gz;
    
    public double hx;
    public double hy;
    public double hz;
}

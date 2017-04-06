package com.dimension4.dcm2stl.logic;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;

public final class STLWriter {
    
    private static final Logger LOGGER = Logger.getLogger(STLWriter.class.getName());

    public static void write(TriangleMesh mesh, File dest){
        write(mesh, dest.getPath());
    }
    
    public static void write(TriangleMesh mesh, String dest){
        LOGGER.log(Level.INFO, "Writing mesh to file {0}", dest);
        
        toxi.geom.mesh.STLWriter writer = new toxi.geom.mesh.STLWriter();
        int numFaces = mesh.getNumFaces();
        
        writer.beginSave(dest, numFaces);
        
        List<Face> faces = mesh.getFaces();
        Iterator<Face> iter = faces.iterator();
        
        while(iter.hasNext()){
            Face face = iter.next();
            writer.face(face.a, face.b, face.c);
        }
        
        writer.endSave();
        
        File test = new File(dest);
        LOGGER.log(Level.INFO, "File generated is {0} bytes", test.length());
    }
}
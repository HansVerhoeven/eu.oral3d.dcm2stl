//package com.dimension4.dcm2stl.logic;
//
//
//import java.io.File;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//
//public class Main {
//
//    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
//
//    public static void main(String[] args) {
//        SysInfoUtil.memInfo();
//
//        float t0 = System.nanoTime();
//        
//        LOGGER.log(Level.INFO, "Running DCM2STL");
//        File dicomDirectory = new File("input");
//        File destination = new File("output/test2.stl");
//        DCM2STL.convert(dicomDirectory, 210, destination);
//        LOGGER.log(Level.INFO, "Done");
//
//        float timeTaken = (System.nanoTime() - t0) * 1e-6f;
//        LOGGER.log(Level.INFO, "Total DICOM to STL conversion took {0}", timeTaken);
//        
//        SysInfoUtil.memInfo();
//    }
//
//    
//}

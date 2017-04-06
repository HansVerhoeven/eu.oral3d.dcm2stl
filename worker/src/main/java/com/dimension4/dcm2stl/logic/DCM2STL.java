package com.dimension4.dcm2stl.logic;

import com.dimension4.dcm2stl.model.CropRegion3D;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.ModalityTransform;
import com.pixelmed.dicom.VOITransform;
import com.pixelmed.display.BufferedImageUtilities;
import com.pixelmed.display.SourceImage;
import com.pixelmed.display.WindowCenterAndWidth;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import toxi.geom.Vec3D;
import toxi.geom.mesh.TriangleMesh;
import toxi.volume.HashIsoSurface;
import toxi.volume.IsoSurface;
import toxi.volume.VolumetricSpaceArray;

public final class DCM2STL {

    private static final Logger LOGGER = Logger.getLogger(DCM2STL.class.getName());

    public static BufferedImage DCM2BufferedImage(byte[] content) {
        LOGGER.info("Converting DCM to BufferedImage");
        BufferedImage resultImage = null;
        DicomInputStream csstream = null;

        try {
            InputStream iStream = new ByteArrayInputStream(content);
            csstream = new DicomInputStream(iStream);
            SourceImage csim = new SourceImage(csstream);
            resultImage = makeEightBitFrame(csim, 0);
            return resultImage;
        } catch (IOException | DicomException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            try {
                csstream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        return resultImage;
    }
    
    // x,y,z*4 -> topRegion
    // x,y,z*4 -> bottomRegion
    // int[0][2] = a, int[1][2] = b, int[2][2] = c, int[3][2] = d (topRegion)
    // int[0][2] = e, int[1][2] = f, int[2][2] = g, int[3][2] = h (bottomRegion)
    public static File convert(List<BufferedImage> dicomList, File destination, CropRegion3D cropRegion) {
        // iterate over buffered images and check for pixel group size
        // when a group is too small, rgb values of the group should be set to black (0)
        // will two passess be necessary? or can we fit this into a single pass?
        Color black = new Color(0, 0, 0);
        int blackRgb = black.getRGB();

        // iterate over all pixels, when a pixel is found that is above the treshold, recursively follow it and count up
        // if the count reaches a certain number, abort and collapse the recursion (eg, return original value)
        // if the count has a small value (3 of 4) and no pixels can be found that are above the treshold
        // collapse the recursion and change the rgb value of the pixels to black
//        dicomList.stream().forEach((dcm) -> {
//            truncGroupsSimple(dcm, treshold);
//        });
//        truncGroups3D(dicomList, treshold);
        // voxelize the pixels of each frame by normalizing the rgb value to a floating point between 0.0 and 1.0
        // all pixels are added as voxels, the meshing algo then filters based on the floating point value that is passed
        // for future extensions, the floating point treshold (ISO_TRESHOLD) passed to the meshing function can be
        // dynamically adapted depending on how many pixels are above a certain value
        int DIMX = dicomList.get(0).getWidth();
        int DIMY = dicomList.get(0).getHeight();
        int DIMZ = dicomList.size();

//        CCL3D(dicomList, DIMX, DIMY, DIMZ);
        int divider = 4;
//        DIMX /= divider;
//        DIMY /= divider;
//        DIMZ /= divider;
//        
        LOGGER.log(Level.INFO, "Dimensions are x:{0} y:{1} z:{2}", new Object[]{DIMX, DIMY, DIMZ});

        float ISO_THRESHOLD = 0.8f;
        float NS = 0.03f;

        Vec3D SCALE = new Vec3D(DIMX, DIMY, DIMZ).scaleSelf(0.25f); //0.25f 0.025 //scaling massively impacts performance!!!!
//        Vec3D SCALE = new Vec3D(DIMX, DIMY, DIMZ).scaleSelf(1f); //0.25f 0.025 //scaling massively impacts performance!!!!

//        Vec3D SCALE = new Vec3D(DIMX/4.0f, DIMY/4.0f, DIMZ/4.0f).scaleSelf(0.025f);
        IsoSurface surface;
        TriangleMesh mesh;

        //reduce the dimensions and the filesize should get better
        VolumetricSpaceArray volume = new VolumetricSpaceArray(SCALE, DIMX, DIMY, DIMZ);
        float normalize = 1 / 255.0f;

        LOGGER.log(Level.INFO, "Generating voxel based representation from BufferedImages");
        for (int z = 0; z < DIMZ; z++) {
//            BufferedImage current = dicomList.get(z);
            BufferedImage current = dicomList.get(DIMZ-z-1);
            for (int y = 0; y < DIMY; y++) {
                for (int x = 0; x < DIMX; x++) {
                    int clr = current.getRGB(x, y);
                    int red = (clr & 0x00ff0000) >> 16;
                    
                    // to introduce cropping, check here if coordinate is within cropping bounds
                    // if it is, set the voxel as normal
                    // if it is not, set the voxel to 0f
                    if(pointInCuboid(x, y, z, cropRegion)) {
                        volume.setVoxelAt(x, y, z, (red < 0 ? 256 + red : red) * normalize);
                    } else {
                        volume.setVoxelAt(x, y, z, 0f);
                    }
                }
            }
        }
        LOGGER.log(Level.INFO, "Done \n\n");

        volume.closeSides();
        long t0 = System.nanoTime();
        // store in IsoSurface and compute surface mesh for the given threshold value
        mesh = new TriangleMesh("iso");
        surface = new HashIsoSurface(volume, 0.01f);// the floating point parameter doesnt really matter
        surface.computeSurfaceMesh(mesh, ISO_THRESHOLD);

        // perform noisefiltering on the mesh
//        VolumeUtil.filterNoise(mesh);
        mesh = VolumeUtil.filterNoise(mesh);

        float timeTaken = (System.nanoTime() - t0) * 1e-6f;
        LOGGER.log(Level.INFO, "Took {0} to compute {1} faces", new Object[]{timeTaken, mesh.getNumFaces()});

        // save the mesh
        STLWriter.write(mesh, destination);
        
        return destination;
    }
    
    // int[0][n] = a, int[1][n] = b, int[2][n] = c, int[3][n] = d (topRegion)
    // int[0][n] = e, int[1][n] = f, int[2][n] = g, int[3][n] = h (bottomRegion)
    private static boolean pointInCuboid(int x, int y, int z, CropRegion3D cropRegion) {
        // from http://math.stackexchange.com/questions/1472049/check-if-a-point-is-inside-a-rectangular-shaped-area-3d
        boolean result;
        
        // z is the vertical axis
        // x is the depth
        // y is the width
        
        
//        x -= DIMX;
//        y -= DIMY;
//        z -= DIMZ;
        // z
        // |  x
        // | /
        // |/___ y
        if(z > cropRegion.az || z < cropRegion.gz) {
            return false;
        }
        if(y < cropRegion.cy || y > cropRegion.dy ) {
            return false;
        }
        if(x < cropRegion.cx || x > cropRegion.ax) {
            return false;
        }
        return true;
        
        
        
        //topRegion
        //a --- b
        //|     |
        //c --- d
        
        //bottomRegion
        //e --- f
        //|     |
        //g --- h
        
        
//        
//        // topRegion
////        Point3d a = new Point3d(topRegion[0][0], topRegion[0][1], topRegion[0][2]);
////        Point3d b = new Point3d(topRegion[1][0], topRegion[1][1], topRegion[1][2]);
//        Point3d c = new Point3d(cropRegion.cx, cropRegion.cy, cropRegion.cz);
////        Point3d c = cropRegion.topRegion.c;
////        Point3d d = new Point3d(topRegion[3][0], topRegion[3][1], topRegion[3][2]);
//        
//        // bottomRegion
//        Point3d e = new Point3d(cropRegion.ex, cropRegion.ey, cropRegion.ez);
////        Point3d e = cropRegion.bottomRegion.a;
////        Point3d f = new Point3d(bottomRegion[1][0], bottomRegion[1][1], bottomRegion[1][2]);
//        Point3d g = new Point3d(cropRegion.gx, cropRegion.gy, cropRegion.gz);
////        Point3d g = cropRegion.bottomRegion.c;
//        Point3d h = new Point3d(cropRegion.hx, cropRegion.hy, cropRegion.hz);
////        Point3d h = cropRegion.bottomRegion.d;
//        
////        u = g - e
////        v = g - h
////        w = g - c
//
//        Vector3d u = new Vector3d(g.x - e.x, g.y - e.y, g.z - e.z);
//        Vector3d v = new Vector3d(g.x - h.x, g.y - h.y, g.z - h.z);
//        Vector3d w = new Vector3d(g.x - c.x, g.y - c.y, g.z - c.z);
//
//        Point3d px = new Point3d(x, y, z);
//        
//        //constraint 1
//        // u.g
//        double ug = u.dot(new Vector3d(g));
//        // u.px
//        double upx = u.dot(new Vector3d(px));
//        // u.e
//        double ue = u.dot(new Vector3d(e));
//        // constriant 1 calc
//        boolean constraintOne = (ug < upx) && (upx < ue);
//        
//        //constraint 2
//        // v.g
//        double vg = v.dot(new Vector3d(g));
//        // v.px
//        double vpx = v.dot(new Vector3d(px));
//        // v.e
//        double ve = v.dot(new Vector3d(e));
//        // constriant 2 calc
//        boolean constraintTwo = (vg < vpx) && (vpx < ve);
//        
//        //constraint 3
//        // w.g
//        double wg = w.dot(new Vector3d(g));
//        // w.px
//        double wpx = w.dot(new Vector3d(px));
//        // w.e
//        double we = w.dot(new Vector3d(e));
//        // constriant 3 calc
//        boolean constraintThree = (wg < wpx) && (wpx < we);
//        
////        constraints
////
////        where px is the point
////
////        dot product
////        u.g < u.px < u.e
////        v.g < v.px < v.h
////        w.g < w.px < w.c
//
//        result = constraintOne && constraintTwo && constraintThree;
//        
//        return result;
    }
    
    public static File convert(List<BufferedImage> dicomList, File destination) {
        // iterate over buffered images and check for pixel group size
        // when a group is too small, rgb values of the group should be set to black (0)
        // will two passess be necessary? or can we fit this into a single pass?
        Color black = new Color(0, 0, 0);
        int blackRgb = black.getRGB();

        // iterate over all pixels, when a pixel is found that is above the treshold, recursively follow it and count up
        // if the count reaches a certain number, abort and collapse the recursion (eg, return original value)
        // if the count has a small value (3 of 4) and no pixels can be found that are above the treshold
        // collapse the recursion and change the rgb value of the pixels to black
//        dicomList.stream().forEach((dcm) -> {
//            truncGroupsSimple(dcm, treshold);
//        });
//        truncGroups3D(dicomList, treshold);
        // voxelize the pixels of each frame by normalizing the rgb value to a floating point between 0.0 and 1.0
        // all pixels are added as voxels, the meshing algo then filters based on the floating point value that is passed
        // for future extensions, the floating point treshold (ISO_TRESHOLD) passed to the meshing function can be
        // dynamically adapted depending on how many pixels are above a certain value
        int DIMX = dicomList.get(0).getWidth();
        int DIMY = dicomList.get(0).getHeight();
        int DIMZ = dicomList.size();

//        CCL3D(dicomList, DIMX, DIMY, DIMZ);
        int divider = 4;
//        DIMX /= divider;
//        DIMY /= divider;
//        DIMZ /= divider;
//        
        LOGGER.log(Level.INFO, "Dimensions are x:{0} y:{1} z:{2}", new Object[]{DIMX, DIMY, DIMZ});

        float ISO_THRESHOLD = 0.8f;
        float NS = 0.03f;

//        Vec3D SCALE = new Vec3D(DIMX, DIMY, DIMZ).scaleSelf(0.25f); //0.25f 0.025 //scaling massively impacts performance!!!!
        Vec3D SCALE = new Vec3D(DIMX, DIMY, DIMZ).scaleSelf(1f); //0.25f 0.025 //scaling massively impacts performance!!!!
//        Vec3D SCALE = new Vec3D(DIMX/4.0f, DIMY/4.0f, DIMZ/4.0f).scaleSelf(0.025f);
        IsoSurface surface;
        TriangleMesh mesh;

        //reduce the dimensions and the filesize should get better
        VolumetricSpaceArray volume = new VolumetricSpaceArray(SCALE, DIMX, DIMY, DIMZ);
        float normalize = 1 / 255.0f;

        LOGGER.log(Level.INFO, "Generating voxel based representation from BufferedImages");
        for (int z = 0; z < DIMZ; z++) {
            BufferedImage current = dicomList.get(z);
            for (int y = 0; y < DIMY; y++) {
                for (int x = 0; x < DIMX; x++) {
                    int clr = current.getRGB(x, y);
                    int red = (clr & 0x00ff0000) >> 16;

                    // to introduce cropping, check here if coordinate is within cropping bounds
                    // if it is, set the voxel as normal
                    // if it is not, set the voxel to 0f
                    volume.setVoxelAt(x, y, z, (red < 0 ? 256 + red : red) * normalize);
                }
            }
        }
        LOGGER.log(Level.INFO, "Done \n\n");

        volume.closeSides();
        long t0 = System.nanoTime();
        // store in IsoSurface and compute surface mesh for the given threshold value
        mesh = new TriangleMesh("iso");
        surface = new HashIsoSurface(volume, 0.01f);// the floating point parameter doesnt really matter
        surface.computeSurfaceMesh(mesh, ISO_THRESHOLD);

        // perform noisefiltering on the mesh
        VolumeUtil.filterNoise(mesh);

        float timeTaken = (System.nanoTime() - t0) * 1e-6f;
        LOGGER.log(Level.INFO, "Took {0} to compute {1} faces", new Object[]{timeTaken, mesh.getNumFaces()});

        // save the mesh
        STLWriter.write(mesh, destination);
        
        return destination;
    }
    
    public static void convert(File directory, File destination) {
        LOGGER.log(Level.INFO, "Converting DCM files from {0} to STL file {1}", new Object[]{directory.getAbsolutePath(), destination.getAbsolutePath()});
        List<BufferedImage> dicomList = new ArrayList<>();
        File[] files = directory.listFiles((File dir1, String name) -> name.endsWith(".dcm"));

        LOGGER.log(Level.INFO, "Converting DCM files to BufferedImages with correct WW/WC settings");
        try {
            for (File f : files) {
                DicomInputStream csstream = null;
//                System.out.println(f);
                csstream = new DicomInputStream(f);
                SourceImage csim = new SourceImage(csstream);
                BufferedImage bi = makeEightBitFrame(csim, 0);

                dicomList.add(bi);
                try {
                    csstream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException | DicomException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        LOGGER.log(Level.INFO, "Done \n\n");

        // iterate over buffered images and check for pixel group size
        // when a group is too small, rgb values of the group should be set to black (0)
        // will two passess be necessary? or can we fit this into a single pass?
        Color black = new Color(0, 0, 0);
        int blackRgb = black.getRGB();
        LOGGER.log(Level.INFO, "Integer value of RGB black is {0}", blackRgb); // once int value known, just set that

        // iterate over all pixels, when a pixel is found that is above the treshold, recursively follow it and count up
        // if the count reaches a certain number, abort and collapse the recursion (eg, return original value)
        // if the count has a small value (3 of 4) and no pixels can be found that are above the treshold
        // collapse the recursion and change the rgb value of the pixels to black
//        dicomList.stream().forEach((dcm) -> {
//            truncGroupsSimple(dcm, treshold);
//        });
//        truncGroups3D(dicomList, treshold);
        // voxelize the pixels of each frame by normalizing the rgb value to a floating point between 0.0 and 1.0
        // all pixels are added as voxels, the meshing algo then filters based on the floating point value that is passed
        // for future extensions, the floating point treshold (ISO_TRESHOLD) passed to the meshing function can be
        // dynamically adapted depending on how many pixels are above a certain value
        int DIMX = dicomList.get(0).getWidth();
        int DIMY = dicomList.get(0).getHeight();
        int DIMZ = dicomList.size();

//        CCL3D(dicomList, DIMX, DIMY, DIMZ);
        int divider = 4;
//        DIMX /= divider;
//        DIMY /= divider;
//        DIMZ /= divider;
//        
        LOGGER.log(Level.INFO, "Dimensions are x:{0} y:{1} z:{2}", new Object[]{DIMX, DIMY, DIMZ});

        float ISO_THRESHOLD = 0.8f;
        float NS = 0.03f;

//        Vec3D SCALE = new Vec3D(DIMX, DIMY, DIMZ).scaleSelf(0.25f); //0.25f 0.025 //scaling massively impacts performance!!!!
        Vec3D SCALE = new Vec3D(DIMX, DIMY, DIMZ).scaleSelf(1f); //0.25f 0.025 //scaling massively impacts performance!!!!
//        Vec3D SCALE = new Vec3D(DIMX/4.0f, DIMY/4.0f, DIMZ/4.0f).scaleSelf(0.025f);
        IsoSurface surface;
        TriangleMesh mesh;

        //reduce the dimensions and the filesize should get better
        VolumetricSpaceArray volume = new VolumetricSpaceArray(SCALE, DIMX, DIMY, DIMZ);
        float normalize = 1 / 255.0f;

        LOGGER.log(Level.INFO, "Generating voxel based representation from BufferedImages");
        for (int z = 0; z < DIMZ; z++) {
            BufferedImage current = dicomList.get(z);
            for (int y = 0; y < DIMY; y++) {
                for (int x = 0; x < DIMX; x++) {
                    int clr = current.getRGB(x, y);
                    int red = (clr & 0x00ff0000) >> 16;

                    // to introduce cropping, check here if coordinate is within cropping bounds
                    // if it is, set the voxel as normal
                    // if it is not, set the voxel to 0f
                    volume.setVoxelAt(x, y, z, (red < 0 ? 256 + red : red) * normalize);
                }
            }
        }
        LOGGER.log(Level.INFO, "Done \n\n");

        volume.closeSides();
        long t0 = System.nanoTime();
        // store in IsoSurface and compute surface mesh for the given threshold value
        mesh = new TriangleMesh("iso");
        surface = new HashIsoSurface(volume, 0.01f);// the floating point parameter doesnt really matter
        surface.computeSurfaceMesh(mesh, ISO_THRESHOLD);

        // perform noisefiltering on the mesh
        VolumeUtil.filterNoise(mesh);

        float timeTaken = (System.nanoTime() - t0) * 1e-6f;
        LOGGER.log(Level.INFO, "Took {0} to compute {1} faces", new Object[]{timeTaken, mesh.getNumFaces()});

        // save the mesh
        STLWriter.write(mesh, destination);
    }

    private static final BufferedImage makeEightBitFrame(SourceImage sImg, int f) {
        BufferedImage useSrcImage = sImg.getBufferedImage(f);
        BufferedImage renderedImage = null;

        if (useSrcImage.getColorModel().getNumComponents() == 1) {
            ModalityTransform modalityTransform = sImg.getModalityTransform();
            VOITransform voiTransform = sImg.getVOITransform();
            boolean signed = sImg.isSigned();
            boolean inverted = sImg.isInverted();
            boolean usePad = sImg.isPadded();
            int pad = sImg.getPadValue();
            double imgMin = sImg.getMinimum();
            double imgMax = sImg.getMaximum();
            int largestGray = sImg.getPaletteColorLargestGray();
            int firstvalueMapped = sImg.getPaletteColorFirstValueMapped();
            int numberOfEntries = sImg.getPaletteColorNumberOfEntries();
            int bitsPerEntry = sImg.getPaletteColorBitsPerEntry();
            short redTable[] = sImg.getPaletteColorRedTable();
            short greenTable[] = sImg.getPaletteColorGreenTable();
            short blueTable[] = sImg.getPaletteColorBlueTable();

            double useSlope = 1;
            double useIntercept = 0;
            if (modalityTransform != null) {
                useSlope = modalityTransform.getRescaleSlope(f);
                useIntercept = modalityTransform.getRescaleIntercept(f);
            }

            double windowWidth = 0;
            double windowCenter = 0;
            if (voiTransform != null && voiTransform.getNumberOfTransforms(f) > 0) {
                windowWidth = voiTransform.getWidth(f, 0);
                windowCenter = voiTransform.getCenter(f, 0);
            }
            if (windowWidth <= 0) {// use supplied window only if there was one, and if its width was not zero (center may legitimately be zero); indeed, it is forbidden to be -ve also
                LOGGER.log(Level.INFO, "statistically derived window: imgMin = {0}", imgMin);
                LOGGER.log(Level.INFO, "statistically derived window: imgMax = {0}", imgMax);
                double ourMin = imgMin * useSlope + useIntercept;
                double ourMax = imgMax * useSlope + useIntercept;
                LOGGER.log(Level.INFO, "statistically derived window: rescaled min = {0}", ourMin);
                LOGGER.log(Level.INFO, "statistically derived window: rescaled max = {0}", ourMax);
                windowWidth = (ourMax - ourMin);
                windowCenter = (ourMax + ourMin) / 2.0;
                LOGGER.log(Level.INFO, "statistically derived center {0} and width {1}", new Object[]{windowCenter, windowWidth});
            }
            LOGGER.log(Level.INFO, "Using rescale slope {0} and intercept {1} and window center {2} and width {3}", new Object[]{useSlope, useIntercept, windowCenter, windowWidth});

            int useVOIFunction = 0;

            renderedImage = (numberOfEntries == 0 || redTable == null)
                    ? (useVOIFunction == 1
                            ? WindowCenterAndWidth.applyWindowCenterAndWidthLogistic(useSrcImage, windowCenter, windowWidth, signed, inverted, useSlope, useIntercept, usePad, pad)
                            : WindowCenterAndWidth.applyWindowCenterAndWidthLinear(useSrcImage, windowCenter, windowWidth, signed, inverted, useSlope, useIntercept, usePad, pad))
                    : WindowCenterAndWidth.applyWindowCenterAndWidthWithPaletteColor(useSrcImage, windowCenter, windowWidth, sImg.isSigned(), inverted, useSlope, useIntercept, usePad, pad,
                            largestGray, bitsPerEntry, numberOfEntries, redTable, greenTable, blueTable);
        } else if (useSrcImage.getColorModel().getNumComponents() == 3) {
            VOITransform voiTransform = sImg.getVOITransform();
            double windowWidth = 0;
            double windowCenter = 0;
            if (voiTransform != null && voiTransform.getNumberOfTransforms(f) > 0) {
                windowWidth = voiTransform.getWidth(f, 0);								// (first) transform
                windowCenter = voiTransform.getCenter(f, 0);
            }
            if (windowWidth <= 0) {// use supplied window only if there was one, and if its width was not zero (center may legitimately be zero); indeed, it is forbidden to be -ve also
                LOGGER.log(Level.INFO, "Color image without windowing");
                renderedImage = BufferedImageUtilities.convertToMostFavorableImageType(useSrcImage);
            } else {
                LOGGER.log(Level.INFO, "Color image with window center {0} and width {1}", new Object[]{windowCenter, windowWidth});
                // No rescaling for color images
                // use only linear voiTransform ... and no rescaling, no sign, no inversion (for now), no padding
                renderedImage = WindowCenterAndWidth.applyWindowCenterAndWidthLinearToColorImage(useSrcImage, windowCenter, windowWidth);
            }
        } else {
            try {
                renderedImage = BufferedImageUtilities.convertToMostFavorableImageType(useSrcImage);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                renderedImage = useSrcImage;
            }
        }
        return renderedImage;
    }
}

package com.dimension4.dcm2stl.model;

import com.dimension4.dcm2stl.logic.DCM2STL;
import com.dimension4.dcm2stl.service.S3BucketService;
import com.dimension4.dcm2stl.service.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;

@Component
public class ConvertAction extends AbstractAction {

//    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ConvertAction.class.getName());
//    
//    @PostConstruct
//    private void init() {
//        LOGGER.log(Level.INFO, "constructed");
//    }
    
    /**
     * Creates action according to Task type and registers it with task executor component
     *
     * @param executor
     */
    @Autowired
    public ConvertAction(TaskExecutor executor) {
        super(TaskType.CONVERT, executor);
    }

    @Override
    public File execute(List<BufferedImage> inputImages, File destination, CropRegion3D cropRegion) {
        File resultFile = null;
        
        //a --- b
        //|     |
        //c --- d
        
        //e --- f
        //|     |
        //g --- h
              
                
        if(cropRegion != null) {
            resultFile = DCM2STL.convert(inputImages, destination, cropRegion);
        } else {
            resultFile = DCM2STL.convert(inputImages, destination);
        }
        return resultFile;
    }
}

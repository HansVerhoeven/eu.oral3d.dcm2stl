package com.dimension4.dcm2stl.model;


import com.dimension4.dcm2stl.service.TaskExecutor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Base class for image processing actions
 * Created by Polina Petrenko on 03.12.2016.
 */
public abstract class AbstractAction {

    /**
     * Creates action according to Task type and registers it with task executor component
     * @param taskType
     * @param executor
     */
    public AbstractAction(TaskType taskType, TaskExecutor executor) {
        executor.registerAction(taskType, this);
    }
    
    public abstract File execute(List<BufferedImage> inputImages, File destination, CropRegion3D cropRegion);
}

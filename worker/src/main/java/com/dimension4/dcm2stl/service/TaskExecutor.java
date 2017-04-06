package com.dimension4.dcm2stl.service;

import com.dimension4.dcm2stl.logic.DCM2STL;
import com.dimension4.dcm2stl.model.AbstractAction;
import com.dimension4.dcm2stl.model.CropRegion3D;
import com.dimension4.dcm2stl.model.Task;
import com.dimension4.dcm2stl.model.TaskStatus;
import com.dimension4.dcm2stl.model.TaskType;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

@Service
public class TaskExecutor {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(TaskExecutor.class.getName());

    @Autowired
    private ITaskRepository taskRepository;
    @Autowired
    private FileStorageService fileStorageService;

    private Map<TaskType, AbstractAction> actionMap = new HashMap<>();

    public void registerAction(TaskType taskType, AbstractAction action) {
        actionMap.put(taskType, action);
    }

    /**
     * Executes task according to task type and output format Does nothing if no
     * task found or no action or output format supported
     *
     * @param taskId
     */
    public void execute(String taskId) {

        LOGGER.info("Method Start: execute");
        Task task = taskRepository.find(taskId);
        if (task == null) {
            LOGGER.severe("Task was not found");
            return;
        }
        task.setStatus(TaskStatus.PROCESSING);
        taskRepository.save(task);

        LOGGER.info("Getting crop data");
//        int[][] topRegion = task.getInput().getTopRegion();
//        int[][] bottomRegion = task.getInput().getBottomRegion();
        CropRegion3D cropRegion = task.getInput().getCropRegion();

        LOGGER.log(Level.INFO, "Received cropregion {0}", cropRegion);

        LOGGER.info("Loading input images");
        List<BufferedImage> inputImages;

        try {
            List<String> imageUrls = getImageUrls(task);
            inputImages = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                BufferedImage fetched = loadImage(imageUrl);
                inputImages.add(fetched);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading input images {0}", e.getMessage());
            return;
        }

        AbstractAction action = actionMap.get(task.getType());
        if (action == null) {
            LOGGER.severe("No action for current task type");
            return;
        }

        LOGGER.info("Performing task");
        File destination = new File(taskId + ".stl");
        File resultFile = action.execute(inputImages, destination, cropRegion);

        try {
            InputStream iStream = new FileInputStream(resultFile);
            String outputUrl = null;
//            if(task.getAwsOwnerId() != null) {
//                outputUrl = fileStorageService.uploadFile(iStream, "file/stl", resultFile.length(), resultFile.getName(), taskId, task.getAwsOwnerId());
//            } else {
//                outputUrl = fileStorageService.uploadFile(iStream, "file/stl", resultFile.length(), resultFile.getName(), taskId);
//            }

//            outputUrl = fileStorageService.uploadFile(iStream, "file/stl", resultFile.length(), resultFile.getName(), taskId, true);
            outputUrl = fileStorageService.uploadFileWithManager(iStream, "file/stl", resultFile.length(), resultFile.getName(), taskId, true);
            //ea80dd5f-f13e-4e5c-99f3-51bcd2ca9590/ea80dd5f-f13e-4e5c-99f3-51bcd2ca9590.stl
            task.setOutputURL(outputUrl);
            task.setStatus(TaskStatus.READY);
            LOGGER.log(Level.INFO, "set task {0} to status {1}", new Object[]{task.getId(), task.getStatus().getName()});
            taskRepository.save(task);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return;
        } finally {
            LOGGER.log(Level.INFO, "Cleaning up file {0}", resultFile.getName());
            if (resultFile.delete()) {
                LOGGER.log(Level.INFO, "Cleaned up file");
            } else {
                LOGGER.log(Level.WARNING, "Could not clean up file");
            }
        }

        LOGGER.info("Method end");
    }

    private List<String> getImageUrls(Task task) {
        List<String> imageUrls = task.getInput().getImages();
        LOGGER.log(Level.INFO, "Sorting {0} images", imageUrls.size());

        // sort the file list by numerical value
        Collections.sort(imageUrls, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Double d1 = extractDouble(o1);
                Double d2 = extractDouble(o2);
                return Double.compare(d1, d2);
            }

            Double extractDouble(String s) {
                String subString = StringUtils.substringAfterLast(s, "/");
                System.out.println("substring after / is " + subString);
                String num = subString.replaceAll("\\D", "");
                // return 0 if no digits found
                System.out.println("found number " + num + " in subString");
                return num.isEmpty() ? 0 : Double.parseDouble(num);
            }
        });
        
        /*
        // Old sorting
        // sort the file list by numerical value
        Collections.sort(imageUrls, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return extractInt(o1) - extractInt(o2);
            }

            int extractInt(String s) {
                String subString = StringUtils.substringAfterLast(s, "/");
                System.out.println("substring after / is " + subString);
                String num = subString.replaceAll("\\D", "");
                // return 0 if no digits found
                System.out.println("found number " + num + " in subString");
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });
        */

        return imageUrls;
    }

    private BufferedImage loadImage(String imageUrl) throws Exception {
        byte[] content = fileStorageService.downloadUrl(imageUrl);
        BufferedImage bufferedImage = DCM2STL.DCM2BufferedImage(content);
        return bufferedImage;
    }

//    private List<BufferedImage> loadImages(List<String> imageUrls) throws Exception {
//        List<BufferedImage> images = new ArrayList<>();
//        for (String url : imageUrls) {
//            byte[] content = fileStorageService.downloadUrl(url);
//            BufferedImage bufferedImage = DCM2STL.DCM2BufferedImage(content);
//            images.add(bufferedImage);
//        }
//        return images;
//    }
}

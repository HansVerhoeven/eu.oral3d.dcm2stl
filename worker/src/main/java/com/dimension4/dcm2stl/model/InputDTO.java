package com.dimension4.dcm2stl.model;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.List;

/**
 * Data transfer object for Input entity
 * Created by Polina Petrenko on 01.12.2016.
 */
public class InputDTO {

    @NotEmpty
    @Valid
    private List<ImageDTO> images;

//    private int[][] topRegion;
//    private int[][] bottomRegion;
    
    private CropRegion3D cropRegion;
    
    public InputDTO() {
    }

    public InputDTO(List<ImageDTO> images) {
        this.images = images;
    }
    
    public InputDTO(List<ImageDTO> images, CropRegion3D cropRegion) {
        this(images);
        this.cropRegion = cropRegion;
    }

    public List<ImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ImageDTO> images) {
        this.images = images;
    }

    public CropRegion3D getCropRegion() {
        return cropRegion;
    }

    public void setCropRegion(CropRegion3D cropRegion) {
        this.cropRegion = cropRegion;
    }
}

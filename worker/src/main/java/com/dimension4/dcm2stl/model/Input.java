package com.dimension4.dcm2stl.model;
import com.dimension4.dcm2stl.service.Util;
import java.util.ArrayList;
import java.util.List;


public class Input {
    
    private String id;

    private List<String> images = new ArrayList<>();
    
    private CropRegion3D cropRegion;

    public Input() {
        this.id = Util.generateId();
    }

    public Input(List<String> images) {
        this();
        this.images = images;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public void setImage(List<ImageDTO> images) {
        for(ImageDTO idto:images) {
            this.images.add(idto.getHref());
        }
    }

    public CropRegion3D getCropRegion() {
        return cropRegion;
    }

    public void setCropRegion(CropRegion3D cropRegion) {
        this.cropRegion = cropRegion;
    }
}

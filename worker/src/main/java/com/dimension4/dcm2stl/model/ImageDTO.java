package com.dimension4.dcm2stl.model;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Data transfer object for images in task input
 * Created by Polina Petrenko on 02.12.2016.
 */
public class ImageDTO {

    @NotBlank
    private String href;

    public ImageDTO() {
    }

    public ImageDTO(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}

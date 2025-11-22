package com.ticketshall.events.validations;

import com.ticketshall.events.exceptions.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

public class ImageValidator implements Validator {
    MultipartFile file;
    boolean update = false; // if we will update the event, it is okay to be optional

    public ImageValidator(MultipartFile file, boolean update) {
        this.file = file;
        this.update = update;
    }

    @Override
    public void validate() {
        if (!update && file.isEmpty()) {
            throw new BadRequestException("Image is required");
        }

        if(!file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("File type must be an image");
            }
        }
    }
}

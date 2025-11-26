package com.ticketshall.venues.validators;


import com.ticketshall.venues.exceptions_handlers.BadRequestException;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
public class ImageValidator implements Validator {
    MultipartFile file;
    boolean update = false;

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

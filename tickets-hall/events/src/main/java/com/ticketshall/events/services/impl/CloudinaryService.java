package com.ticketshall.events.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", "events"
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("image uploading failed:" + e.getMessage());
        }
    }

    public void deleteImage(String imageUrl) {
        String publicId = getPublicIdFromUrl(imageUrl);
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException error) {
            throw new RuntimeException("image deletion failed: " + error.getMessage());
        }
    }

    private String getPublicIdFromUrl(String imageUrl) {
        Pattern pattern = Pattern.compile("upload/(?:v\\d+/)?([^.]+)\\.[^.]+$");
        Matcher matcher = pattern.matcher(imageUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid Cloudinary URL: " + imageUrl);
    }
}
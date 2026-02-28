package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file){
        try{
            if (file.isEmpty()) {
                throw new AppException(ErrorCode.FILE_EMPTY);
            }
            String publicValue = generatePublicValue(file.getOriginalFilename());

            var uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicValue,
                    "resource_type", "auto",
                    "folder", "resume"
            ));
            return String.valueOf(uploadResult.get("secure_url"));
        }catch (Exception e){
            throw new AppException(ErrorCode.FILE_ERROR);
        }

    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file));
        }
        return urls;
    }

    public String generatePublicValue(String originalName){
        String fileName = getFileName(originalName);
        String cleanFileName = fileName.replaceAll("\\s+", "_").toLowerCase();
        return UUID.randomUUID().toString()+"_"+cleanFileName;
    }

    public String getFileName(String originalName){
        if( originalName==null || originalName.isEmpty()){
            return "unknown";
        }
        int index = originalName.lastIndexOf('.');
        if(index==-1){
            return originalName;
        }
        return originalName.substring(0, index);
    }
}

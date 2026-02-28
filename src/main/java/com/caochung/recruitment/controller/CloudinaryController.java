package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class CloudinaryController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/uploads")
    @PreAuthorize(SecurityConstant.FILE_UPLOAD)
    public ResponseData<?> uploadFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
        List<String> urls = cloudinaryService.uploadFiles(files);
        return ResponseData.success(urls ,SuccessCode.UPLOAD_SUCCESS);
    }

    @PostMapping("/upload")
    @PreAuthorize(SecurityConstant.FILE_UPLOAD)
    public ResponseData<?> uploadFile(@RequestParam("file") MultipartFile file){
        String url = cloudinaryService.uploadFile(file);
        return ResponseData.success(url ,SuccessCode.UPLOAD_SUCCESS);
    }
}

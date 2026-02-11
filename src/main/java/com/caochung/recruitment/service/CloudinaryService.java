package com.caochung.recruitment.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CloudinaryService {
    String uploadFile(MultipartFile file);

    List<String> uploadFiles(MultipartFile[] files) throws IOException;
}

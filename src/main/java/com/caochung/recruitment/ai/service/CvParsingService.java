package com.caochung.recruitment.ai.service;

import com.caochung.recruitment.ai.dto.ParsedCvDTO;

public interface CvParsingService {
    ParsedCvDTO parse(byte[] pdfBytes);
}

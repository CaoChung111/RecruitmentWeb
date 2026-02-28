package com.caochung.recruitment.service;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.exception.AppException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CompanyService {
    CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO);

    PaginationResponseDTO getAllCompanies(Specification<Company> specification, Pageable pageable);

    CompanyResponseDTO getCompanyById(Long id);

    void updateCompany(Long id, CompanyRequestDTO requestDTO);

    void deleteCompany(Long id);
}

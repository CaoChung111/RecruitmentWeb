package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.service.impl.CompanyServiceImpl;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class CompanyController {
    private final CompanyServiceImpl companyServiceImpl;

    public CompanyController(CompanyServiceImpl companyServiceImpl) {
        this.companyServiceImpl = companyServiceImpl;
    }

    @PostMapping(value = "/companies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<CompanyResponseDTO> createCompany(
            @Valid @RequestPart("company") CompanyRequestDTO companyRequestDTO,
            @RequestPart("file") MultipartFile file){
        CompanyResponseDTO company = this.companyServiceImpl.createCompany(companyRequestDTO, file);
        return ResponseData.success(company, SuccessCode.CREATED_SUCCESS);
    }

    @GetMapping("/companies/{id}")
    public ResponseData<CompanyResponseDTO> getCompanyById(@PathVariable Long id) {
        CompanyResponseDTO company = this.companyServiceImpl.getCompanyById(id);
        return ResponseData.success(company, SuccessCode.GET_SUCCESS);
    }

    @GetMapping("/companies")
    public ResponseData<PaginationResponseDTO> getAllCompanies(
            @Filter Specification<Company> specification,
            Pageable pageable) {

        PaginationResponseDTO resultPagination= this.companyServiceImpl.getAllCompanies(specification, pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @PutMapping("/companies/{id}")
    public ResponseData<?> updateCompany(@PathVariable Long id ,@Valid @RequestBody CompanyRequestDTO companyRequestDTO) {
        this.companyServiceImpl.updateCompany(id, companyRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseData<?> deleteCompany(@PathVariable Long id) {
        this.companyServiceImpl.deleteCompany(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}

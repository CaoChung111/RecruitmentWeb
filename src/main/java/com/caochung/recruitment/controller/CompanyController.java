package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.domain.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.domain.dto.response.ResponseData;
import com.caochung.recruitment.domain.dto.response.ResultPaginationDTO;
import com.caochung.recruitment.service.CompanyService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/companies")
    public ResponseData<CompanyResponseDTO> createCompany(@Valid @RequestBody CompanyRequestDTO companyRequestDTO) {
        CompanyResponseDTO company = this.companyService.createCompany(companyRequestDTO);
        return ResponseData.success(company, SuccessCode.CREATED_SUCCESS);
    }

    @GetMapping("/companies")
    public ResponseData<ResultPaginationDTO> getAllCompanies(
            @Filter Specification<Company> specification,
            Pageable pageable) {

        ResultPaginationDTO resultPagination= this.companyService.getAllCompanies(specification, pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @PutMapping("/companies/{id}")
    public ResponseData updateCompany(@PathVariable Long id ,@Valid @RequestBody CompanyRequestDTO companyRequestDTO) {
        this.companyService.updateCompany(id, companyRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseData deleteCompany(@PathVariable Long id) {
        this.companyService.deleteCompany(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }
}

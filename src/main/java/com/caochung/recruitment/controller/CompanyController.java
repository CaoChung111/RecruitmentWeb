package com.caochung.recruitment.controller;

import com.caochung.recruitment.constant.SecurityConstant;
import com.caochung.recruitment.constant.SuccessCode;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.ResponseData;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.service.CompanyService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Company Management", description = "APIs for managing company profiles and information.")
public class CompanyController {
    private final CompanyService companyService;

    @Operation(summary = "Create a new company", description = "Registers a new company profile with the provided details. Requires 'COMPANY_CREATE' permission.")
    @PostMapping("/companies")
    @PreAuthorize(SecurityConstant.COMPANY_CREATE)
    public ResponseData<CompanyResponseDTO> createCompany(
            @Valid @RequestBody CompanyRequestDTO companyRequestDTO) {
        CompanyResponseDTO company = this.companyService.createCompany(companyRequestDTO);
        return ResponseData.success(company, SuccessCode.CREATED_SUCCESS);
    }

    @Operation(summary = "Get company by ID", description = "Fetches detailed information for a specific company using its unique identifier. Accessible to all authenticated users.")
    @GetMapping("/companies/{id}")
    public ResponseData<CompanyResponseDTO> getCompanyById(@PathVariable Long id) {
        CompanyResponseDTO company = this.companyService.getCompanyById(id);
        return ResponseData.success(company, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Get all companies", description = "Retrieves a paginated list of all companies, with optional filtering. Accessible to all authenticated users.")
    @GetMapping("/companies")
    public ResponseData<PaginationResponseDTO> getAllCompanies(
            @Filter Specification<Company> specification,
            Pageable pageable) {

        PaginationResponseDTO resultPagination= this.companyService.getAllCompanies(specification, pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Update an existing company", description = "Modifies the details of an existing company identified by its ID. Requires 'COMPANY_UPDATE' permission.")
    @PutMapping("/companies/{id}")
    @PreAuthorize(SecurityConstant.COMPANY_UPDATE)
    public ResponseData<?> updateCompany(@PathVariable Long id ,@Valid @RequestBody CompanyRequestDTO companyRequestDTO) {
        this.companyService.updateCompany(id, companyRequestDTO);
        return ResponseData.success(SuccessCode.PUT_SUCCESS);
    }

    @Operation(summary = "Delete a company", description = "Removes a company profile permanently using its unique identifier. Requires 'COMPANY_DELETE' permission.")
    @DeleteMapping("/companies/{id}")
    @PreAuthorize(SecurityConstant.COMPANY_DELETE)
    public ResponseData<?> deleteCompany(@PathVariable Long id) {
        this.companyService.deleteCompany(id);
        return ResponseData.success(SuccessCode.DELETE_SUCCESS);
    }

    @Operation(summary = "Get all inactive companies", description = "Retrieves a paginated list of all inactive companies. Requires 'COMPANY_VIEW_INACTIVE' permission.")
    @GetMapping("/companies/trash")
    @PreAuthorize(SecurityConstant.COMPANY_VIEW_INACTIVE)
    public ResponseData<PaginationResponseDTO> getAllInactiveCompanies(Pageable pageable) {
        PaginationResponseDTO resultPagination= this.companyService.getAllInactiveCompanies(pageable);
        return ResponseData.success(resultPagination, SuccessCode.GET_SUCCESS);
    }

    @Operation(summary = "Restore a company", description = "Restore a company. Requires 'COMPANY_RESTORE' permission.")
    @PutMapping("/companies/{id}/restore")
    @PreAuthorize(SecurityConstant.COMPANY_RESTORE)
    public ResponseData<?> restoreUser(@PathVariable Long id) {
        this.companyService.restoreCompanyById(id);
        return ResponseData.success(SuccessCode.RESTORE_SUCCESS);
    }
}

package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.CompanyService;
import com.caochung.recruitment.service.mapper.CompanyMapper;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;

    @Override
    @Transactional
    public CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO){
        if(companyRepository.existsByName(requestDTO.getName())){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }
        Company company = companyMapper.toEntity(requestDTO);
        company.setStatus(CompanyStatusEnum.ACTIVE);
        Company saveCompany = this.companyRepository.save(company);
        return companyMapper.toDto(saveCompany);
    }

    @Override
    public PaginationResponseDTO getAllCompanies(Specification<Company> specification, Pageable pageable) {
        Page<Company> pageCompany = this.companyRepository.findAll(specification, pageable);
        List<Company> companyEntities = pageCompany.getContent();
        List<CompanyResponseDTO> responseDTOs = companyMapper.toDto(companyEntities);

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(pageCompany.getTotalPages())
                .totalItems(pageCompany.getTotalElements())
                .build();

        return new PaginationResponseDTO(meta, responseDTOs);
    }

    @Override
    @Cacheable(value = "company_detail", key = "#id")
    public CompanyResponseDTO getCompanyById(Long id){
        Company company = this.companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        return companyMapper.toDto(company);
    }

    @Override
    @Transactional
    @CacheEvict(value = "company_detail", key = "#id")
    public void updateCompany(Long id, CompanyRequestDTO requestDTO) {
        Company company = this.companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        if(!requestDTO.getName().equals(company.getName())
                && companyRepository.existsByName(requestDTO.getName())){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }
        companyMapper.fromUpdateDto(requestDTO, company);
    }

    @Override
    @Transactional
    @CacheEvict(value = "company_detail", key = "#id")
    public void deleteCompany(Long id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        Instant now =  Instant.now();
        String currentUser = SecurityUtil.getCurrentUserLogin().orElse("SYSTEM");
        jobRepository.inactivateJobsByCompanyId(now, currentUser, company.getId());
        resumeRepository.inactivateResumeByCompanyId(now, currentUser, company.getId());
        userRepository.inactivateUsersByCompanyId(now, currentUser, company.getId());
        companyRepository.delete(company);
    }

    @Override
    public PaginationResponseDTO getAllInactiveCompanies(Pageable pageable) {
        Page<Company> pageCompany = this.companyRepository.findAllInactiveCompanies(pageable);
        List<CompanyResponseDTO> responseDTOs = companyMapper.toDto(pageCompany.getContent());

        PaginationResponseDTO.Meta meta = PaginationResponseDTO.Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPages(pageCompany.getTotalPages())
                .totalItems(pageCompany.getTotalElements())
                .build();

        return new PaginationResponseDTO(meta, responseDTOs);
    }

    @Transactional
    @Override
    public void restoreCompanyById(Long id){
        Company company = companyRepository.findByIdIncludingDeleted(id).orElseThrow(
                () ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        if (company.getStatus().equals(CompanyStatusEnum.ACTIVE)){
            throw new AppException(ErrorCode.COMPANY_ALREADY_ACTIVE);
        }
        jobRepository.restoreJobsByCompanyId(company.getId());
        userRepository.restoreUsersByCompanyId(company.getId());
        companyRepository.restoreCompanyById(company.getId());
    }
}

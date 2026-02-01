package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.mapper.CompanyMapper;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;

    public CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO) {
        if(companyRepository.existsByName(requestDTO.getName())){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }
        Company company = companyMapper.toEntity(requestDTO);
        Company saveCompany = this.companyRepository.save(company);
        return companyMapper.toDto(saveCompany);
    }

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

    public CompanyResponseDTO getCompanyById(Long id){
        Company company = this.companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        return companyMapper.toDto(company);
    }

    public void updateCompany(Long id, CompanyRequestDTO requestDTO) {
        Company company = this.companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        if(!requestDTO.getName().equals(company.getName())
                && companyRepository.existsByName(requestDTO.getName())){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }
        companyMapper.fromUpdateDto(requestDTO, company);
        this.companyRepository.save(company);
    }

    public void deleteCompany(Long id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        userRepository.deleteAll(userRepository.findAllByCompany(company));
        companyRepository.delete(company);
    }
}

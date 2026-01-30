package com.caochung.recruitment.service;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.domain.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.domain.dto.response.Meta;
import com.caochung.recruitment.domain.dto.response.ResultPaginationDTO;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.service.mapper.CompanyMapper;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.constant.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    public CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO) {
        if(companyRepository.existsByName(requestDTO.getName())){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }
        Company company = companyMapper.toEntity(requestDTO);
        Company saveCompany = this.companyRepository.save(company);
        CompanyResponseDTO responseDTO = companyMapper.toDto(saveCompany);
        return responseDTO;
    }

    public ResultPaginationDTO getAllCompanies(Specification<Company> specification, Pageable pageable) {
        Page<Company> pageCompany = this.companyRepository.findAll(specification, pageable);
        List<Company> companyEntities = pageCompany.getContent();
        List<CompanyResponseDTO> responseDTOs = companyMapper.toDto(companyEntities);

        Meta meta = Meta.builder()
                .page(pageable.getPageNumber()+1)
                .pageSize(pageable.getPageSize())
                .totalPage(pageCompany.getTotalPages())
                .totalItems(pageCompany.getTotalElements())
                .build();

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO(meta, responseDTOs);
        return resultPaginationDTO;
    }

    public CompanyResponseDTO getCompanyById(Long id){
        Company company = this.companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        CompanyResponseDTO companyResponseDTO = companyMapper.toDto(company);
        return companyResponseDTO;
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
        companyRepository.delete(company);
    }
}

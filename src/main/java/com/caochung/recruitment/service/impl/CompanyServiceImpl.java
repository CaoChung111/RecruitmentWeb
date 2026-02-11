package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.dto.request.CompanyRequestDTO;
import com.caochung.recruitment.dto.response.CompanyResponseDTO;
import com.caochung.recruitment.dto.response.PaginationResponseDTO;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.service.CloudinaryService;
import com.caochung.recruitment.service.CompanyService;
import com.caochung.recruitment.service.mapper.CompanyMapper;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO requestDTO, MultipartFile imageFile){
        if(companyRepository.existsByName(requestDTO.getName())){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }
        requestDTO.setLogo(cloudinaryService.uploadFile(imageFile));
        Company company = companyMapper.toEntity(requestDTO);
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
    public CompanyResponseDTO getCompanyById(Long id){
        Company company = this.companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        return companyMapper.toDto(company);
    }

    @Override
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

    @Override
    public void deleteCompany(Long id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() ->  new AppException(ErrorCode.COMPANY_NOT_FOUND));
        userRepository.deleteAll(userRepository.findAllByCompany(company));
        companyRepository.delete(company);
    }
}

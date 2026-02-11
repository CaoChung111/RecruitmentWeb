package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.dto.request.ResumeRequestDTO;
import com.caochung.recruitment.dto.request.ResumeUpdateDTO;
import com.caochung.recruitment.dto.response.ResumeResponseDTO;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ResumeMapper {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Mapping(source = "userId", target = "user")
    @Mapping(source = "jobId", target = "job")
    public abstract Resume toResume(ResumeRequestDTO resumeRequestDTO);

    @Mapping(source = "job.company.name", target = "companyName")
    public abstract ResumeResponseDTO toDTO(Resume resume);
    public abstract ResumeResponseDTO.UserResume toDTO(User user);
    public abstract ResumeResponseDTO.JobResume toDTO(Job job);

    public abstract void fromUpdate(ResumeUpdateDTO resumeUpdateDTO,@MappingTarget Resume resume);

    public abstract List<ResumeResponseDTO> toDTO(List<Resume> resumes);

    protected User mapUser(Long id){
        if(id==null){
            return null;
        }
        return userRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    protected Job mapJob(Long id){
        if(id==null){
            return null;
        }
        return jobRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.JOB_NOT_FOUND));
    }
}

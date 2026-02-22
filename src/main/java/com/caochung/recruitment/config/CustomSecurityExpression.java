package com.caochung.recruitment.config;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.domain.Job;
import com.caochung.recruitment.domain.Resume;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("customSecurity")
@RequiredArgsConstructor
public class CustomSecurityExpression {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final CompanyRepository companyRepository;

    // Check quyền User
    public boolean isUserOwner (Long id){
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ?
                SecurityUtil.getCurrentUserLogin().get() : "";

        User user = userRepository.findByEmail(email);
        if(user == null){
            return false;
        }

        if(user.getRole().getName().equals("SUPER_ADMIN")){
            return true;
        }
        return user.getId().equals(id);
    }

    // Check quyền sở hữu Job (ADMIN và RECRUITER)
    public boolean isJobOwner(Long id){
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ?
                SecurityUtil.getCurrentUserLogin().get() : "";

        User currentUser = userRepository.findByEmail(email);
        if(currentUser == null){
            return false;
        }

        if(currentUser.getRole().getName().equals("SUPER_ADMIN")){
            return true;
        }

        Optional<Job> job = jobRepository.findById(id);
        if(job.isEmpty()) return false;
        if (currentUser.getCompany() == null) return false;
        return job.get().getCompany().getId().equals(currentUser.getCompany().getId());
    }

    // Check quyền sở hữu Resume (CANDIDATE và ADMIN)
    public boolean isResumeOwner(Long id){
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentUser = userRepository.findByEmail(email);
        if(currentUser == null) return false;

        if(currentUser.getRole().getName().equals("SUPER_ADMIN")) return true;

        Optional<Resume> resume = resumeRepository.findById(id);
        if(resume.isEmpty()) return false;

        return resume.get().getUser().getId().equals(currentUser.getId());
    }

    // Check quyền HR với Resume (HR và ADMIN)
    public boolean isResumeInRecruiterCompany(Long resumeId) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User currentUser = userRepository.findByEmail(email);
        if(currentUser == null) return false;

        if(currentUser.getRole().getName().equals("SUPER_ADMIN")) return true;

        if (currentUser.getCompany() == null) return false;

        Optional<Resume> resume = resumeRepository.findById(resumeId);
        if (resume.isEmpty()) return false;

        Company resumeCompany = resume.get().getJob().getCompany();
        return resumeCompany != null && resumeCompany.getId().equals(currentUser.getCompany().getId());
    }


    // Check quyền sở hữu Company (RECRUITER update thông tin công ty)
    public boolean isCompanyOwner(Long id){
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ?
                SecurityUtil.getCurrentUserLogin().get() : "";

        User currentUser = userRepository.findByEmail(email);
        if(currentUser == null){
            return false;
        }

        if(currentUser.getRole().getName().equals("SUPER_ADMIN")){
            return true;
        }
        if (currentUser.getCompany() == null) return false;
        return currentUser.getCompany().getId().equals(id);
    }
}

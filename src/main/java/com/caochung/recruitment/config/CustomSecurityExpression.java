package com.caochung.recruitment.config;

import com.caochung.recruitment.constant.UserStatusEnum;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("customSecurity")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomSecurityExpression {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final CompanyRepository companyRepository;

    // Lấy User hợp lệ
    private User getValidCurrentUser() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        if (email.isEmpty()) return null;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !UserStatusEnum.ACTIVE.equals(user.getStatus())) {
            return null;
        }
        return user;
    }

    // Check quyền User
    public boolean isUserOwner(Long userId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;

        if (currentUser.getRole().getName().equals("SUPER_ADMIN")) return true;

        return currentUser.getId().equals(userId);
    }

    // Check quyền sở hữu Job
    public boolean isJobOwner(Long jobId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole().getName().equals("SUPER_ADMIN")) return true;

        Optional<Job> job = jobRepository.findById(jobId);
        if (job.isEmpty()) return false;

        if (currentUser.getCompany() == null) return false;
        return job.get().getCompany().getId().equals(currentUser.getCompany().getId());
    }

    // Check quyền sở hữu Resume (Ứng viên xem/rút CV)
    public boolean isResumeOwner(Long resumeId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole().getName().equals("SUPER_ADMIN")) return true;

        Optional<Resume> resume = resumeRepository.findById(resumeId);
        if (resume.isEmpty()) return false;

        return resume.get().getUser().getId().equals(currentUser.getId());
    }

    // Check quyền HR với Resume
    public boolean isResumeInRecruiterCompany(Long resumeId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole().getName().equals("SUPER_ADMIN")) return true;
        if (currentUser.getCompany() == null) return false;

        Optional<Resume> resume = resumeRepository.findById(resumeId);
        if (resume.isEmpty()) return false;

        Company resumeCompany = resume.get().getJob().getCompany();
        return resumeCompany != null && resumeCompany.getId().equals(currentUser.getCompany().getId());
    }

    // Check quyền sở hữu Company
    public boolean isCompanyOwner(Long companyId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (currentUser.getRole().getName().equals("SUPER_ADMIN")) return true;

        if (currentUser.getCompany() == null) return false;
        return currentUser.getCompany().getId().equals(companyId);
    }
}
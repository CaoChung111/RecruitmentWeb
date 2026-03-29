package com.caochung.recruitment.config;

import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.repository.CompanyRepository;
import com.caochung.recruitment.repository.JobRepository;
import com.caochung.recruitment.repository.ResumeRepository;
import com.caochung.recruitment.repository.UserRepository;
import com.caochung.recruitment.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("customSecurity")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomSecurityExpression {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final CompanyRepository companyRepository;

    // Lấy User hợp lệ (Tạm giữ nguyên, nhưng đã gom logic check cho gọn)
    private User getValidCurrentUser() {
        return SecurityUtil.getCurrentUserLogin()
                .flatMap(userRepository::findByEmail)
                .filter(user -> UserStatusEnum.ACTIVE.equals(user.getStatus()))
                .orElse(null);
    }

    private boolean isSuperAdmin(User user) {
        return user.getRole().getName().equals("SUPER_ADMIN");
    }

    // 1. Check quyền User
    public boolean isUserOwner(Long userId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;

        return isSuperAdmin(currentUser) || currentUser.getId().equals(userId);
    }

    // 2. Check quyền sở hữu Job (HR check Job của công ty mình)
    public boolean isJobOwner(Long jobId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (isSuperAdmin(currentUser)) return true;

        if (currentUser.getCompany() == null) return false;

        // Tối ưu: Dùng exists (Chỉ SELECT 1 record) thay vì load cả entity Job
        return jobRepository.existsByIdAndCompany_Id(jobId, currentUser.getCompany().getId());
    }

    // 3. Check quyền sở hữu Resume (Ứng viên thao tác với CV của mình)
    public boolean isResumeOwner(Long resumeId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (isSuperAdmin(currentUser)) return true;

        // Tối ưu: Dùng exists
        return resumeRepository.existsByIdAndUser_Id(resumeId, currentUser.getId());
    }

    // 4. Check quyền HR với Resume (HR chỉ được xem CV nộp vào công ty mình)
    public boolean isResumeInRecruiterCompany(Long resumeId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (isSuperAdmin(currentUser)) return true;
        if (currentUser.getCompany() == null) return false;

        // Tối ưu đỉnh cao: Xuyên qua 2 bảng (Resume -> Job -> Company) chỉ bằng 1 hàm exists
        return resumeRepository.existsByIdAndJob_Company_Id(resumeId, currentUser.getCompany().getId());
    }

    // 5. Check quyền sở hữu Company
    public boolean isCompanyOwner(Long companyId) {
        User currentUser = getValidCurrentUser();
        if (currentUser == null) return false;
        if (isSuperAdmin(currentUser)) return true;

        return currentUser.getCompany() != null && currentUser.getCompany().getId().equals(companyId);
    }
}
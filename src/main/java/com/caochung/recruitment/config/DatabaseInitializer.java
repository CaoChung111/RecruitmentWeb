package com.caochung.recruitment.config;

import com.caochung.recruitment.constant.PermissionEnum;
import com.caochung.recruitment.domain.Permission;
import com.caochung.recruitment.domain.Role;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.repository.PermissionRepository;
import com.caochung.recruitment.repository.RoleRepository;
import com.caochung.recruitment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("RUN FAKE DATABASE INITIALIZER");
        List<Permission> allPermissions = seedPermissions();
        seedRoles(allPermissions);
        seedUsers();
        System.out.println("RUN FAKE DATABASE INITIALIZER");
    }

    private List<Permission> seedPermissions() {
        if (this.permissionRepository.count() > 0) {
            return this.permissionRepository.findAll();
        }

        ArrayList<Permission> arr = new ArrayList<>();

        for (PermissionEnum p : PermissionEnum.values()) {
            Permission permission = new Permission();
            permission.setName(p.getName());
            permission.setApiPath(p.getApiPath());
            permission.setMethod(p.getMethod());
            permission.setModule(p.getModule());

            arr.add(permission);
        }
        return this.permissionRepository.saveAll(arr);
    }

    private void seedRoles(List<Permission> allPermissions) {
        // --- 1. SUPER_ADMIN ---
        if (this.roleRepository.findByName("SUPER_ADMIN") == null) {
            Role admin = new Role();
            admin.setName("SUPER_ADMIN");
            admin.setDescription("Quản trị viên hệ thống - Full Quyền");
            admin.setActive(true);
            admin.setPermissions(new HashSet<>(allPermissions));
            this.roleRepository.save(admin);
            System.out.println(">>> Initialized ROLE: SUPER_ADMIN");
        }

        // --- 2. CANDIDATE ---
        if (this.roleRepository.findByName("CANDIDATE") == null) {
            Role candidate = new Role();
            candidate.setName("CANDIDATE");
            candidate.setDescription("Ứng viên tìm việc");
            candidate.setActive(true);

            List<String> candidatePermissions = List.of(
                    // Resumes: Quản lý hồ sơ CÁ NHÂN
                    "RESUME_CREATE",
                    "RESUME_DELETE",
                    "RESUME_VIEW_OWN",
                    "RESUME_VIEW_DETAIL",

                    // Users: Quản lý tài khoản
                    "USER_UPDATE",
                    "USER_VIEW_DETAIL",

                    // Files: Upload CV
                    "FILE_UPLOAD",

                    // Subscribers: Nhận tin tuyển dụng
                    "SUBSCRIBER_CREATE"
            );

            List<Permission> permissions = allPermissions.stream()
                    .filter(p -> candidatePermissions.contains(p.getName()))
                    .toList();

            candidate.setPermissions(new HashSet<>(permissions));
            this.roleRepository.save(candidate);
            System.out.println(">>> Initialized ROLE: CANDIDATE");
        }

        // --- 3. RECRUITER ---
        if (this.roleRepository.findByName("RECRUITER") == null) {
            Role recruiter = new Role();
            recruiter.setName("RECRUITER");
            recruiter.setDescription("Nhà tuyển dụng - Đăng tin & Xem hồ sơ");
            recruiter.setActive(true);
            List<String> recruiterPermissions = List.of(
                    // Jobs: Quản lý tin tuyển dụng
                    "JOB_CREATE",
                    "JOB_UPDATE",
                    "JOB_DELETE",

                    // Companies: Quản lý thông tin công ty
                    "COMPANY_UPDATE",

                    // Resumes: Xem ứng viên
                    "RESUME_VIEW_COMPANY",
                    "RESUME_VIEW_DETAIL",
                    "RESUME_UPDATE",

                    // Users: Quản lý tài khoản
                    "USER_UPDATE",
                    "USER_VIEW_DETAIL",

                    // Files: Upload Logo cty
                    "FILE_UPLOAD"
            );

            List<Permission> permissions = allPermissions.stream()
                    .filter(p -> recruiterPermissions.contains(p.getName()))
                    .toList();
            recruiter.setPermissions(new HashSet<>(allPermissions));
            this.roleRepository.save(recruiter);
            System.out.println(">>> Initialized ROLE: RECRUITER");
        }
    }

    private void seedUsers() {
        Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");

        if (this.userRepository.count() == 0 && adminRole != null) {
            User user = new User();
            user.setEmail("admin@gmail.com");
            user.setName("Super Admin");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRole(adminRole);

            this.userRepository.save(user);
            System.out.println(">>> Initialized USER: admin@gmail.com");
        }
    }
}

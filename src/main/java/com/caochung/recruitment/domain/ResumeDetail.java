package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.AnalysisStatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDetail extends Base {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", unique = true, nullable = false)
    private Resume resume;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;               // Lưu JSON chuỗi List<String>

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "current_position")
    private String currentPosition;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "education")
    private String education;

    @Column(name = "experiences", columnDefinition = "TEXT")
    private String experiences;          // Lưu JSON chuỗi List<ExperienceDTO>

    @Column(name = "analysis_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AnalysisStatusEnum analysisStatus;
}
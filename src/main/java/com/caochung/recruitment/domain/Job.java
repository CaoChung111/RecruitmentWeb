package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.JobStatusEnum;
import com.caochung.recruitment.constant.LevelEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter @Setter
public class Job extends Base{
    @Column(name = "name")
    private String name;

    @Column(name = "location")
    private String location;

    @Column(name = "salary")
    private Double salary;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private LevelEnum level;

    @Column(name = "description", columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "active")
    @Enumerated(EnumType.STRING)
    private JobStatusEnum active;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_skill",
    joinColumns = @JoinColumn(name = "job_id"),
    inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
    private List<Resume> resumes ;
}

package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Table(name = "companies")
@Getter @Setter
@SQLDelete(sql = "UPDATE companies SET status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "status != 'INACTIVE'")
public class Company extends Base {
    @Column(name = "name")
    private String name;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "logo")
    private String logo;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CompanyStatusEnum status;

    @OneToMany(mappedBy = "company",  fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Job> jobs;
}

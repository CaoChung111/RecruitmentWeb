package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.ResumeStatusEnum;
import com.caochung.recruitment.dto.response.ResponseData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.WhereJoinTable;

@Entity
@Table(name = "resumes")
@Getter @Setter
@SQLDelete(sql = "UPDATE resumes SET status = 'SYSTEM_CANCEL' WHERE id = ?")
//@Where(clause = "status != 'SYSTEM_CANCEL'")
public class Resume extends Base{
    @Column(name = "email")
    private String email;

    @Column(name = "url")
    private String url;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ResumeStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

}

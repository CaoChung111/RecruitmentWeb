package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.GenderEnum;
import com.caochung.recruitment.constant.UserStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Table(name="users")
@Getter
@Setter
@SQLDelete(sql = "UPDATE users SET status = 'DISABLED', updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "status != 'DISABLED'")
public class User extends Base{
    @Column(name = "name")
    private  String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "age")
    private int age;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @Column(name = "address")
    private String address;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatusEnum status;

    @Column(name = "refresh_token",columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Resume> resumes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
}

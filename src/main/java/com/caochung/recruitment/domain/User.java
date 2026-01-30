package com.caochung.recruitment.domain;

import com.caochung.recruitment.constant.GenderEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="users")
@Getter
@Setter
public class User extends Base{
    private  String name;

    private String email;

    private String password;

    private int age;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    private String address;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

}

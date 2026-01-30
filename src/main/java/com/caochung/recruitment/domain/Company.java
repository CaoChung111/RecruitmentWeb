package com.caochung.recruitment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter @Setter
public class Company extends Base {
    @Column(name = "name")
    private String name;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "logo")
    private String logo;


}

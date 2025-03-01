package com.eaa.identity.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ref_user", uniqueConstraints = @UniqueConstraint(columnNames = "email_address"))
@Data
@NoArgsConstructor
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column(name = "email_address", nullable = false)
    private String email;

    @Column(length = 60)
    private String password;

    private String roles;

    public UserInfo(String username, String email, String password, String roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
}

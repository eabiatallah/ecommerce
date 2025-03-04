package com.eaa.identity.entity;

import com.eaa.identity.constants.Constants;
import com.eaa.identity.utils.ServiceUtils;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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

    private boolean enabled = false;

    private String verificationCode;

    private Date expirationTime;

    public UserInfo(String username, String email, String password, String roles, String verificationCode) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.verificationCode = verificationCode;
        this.expirationTime = ServiceUtils.calculateExpirationDate(Constants.EXPIRATION_TIME);
    }
}

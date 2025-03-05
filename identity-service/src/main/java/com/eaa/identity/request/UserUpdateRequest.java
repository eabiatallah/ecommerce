package com.eaa.identity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserUpdateRequest {

    private String username;
    private String email;
    private String password;
    private String role;
}

package com.example.projet2024.DTO;


import com.example.projet2024.Enum.Role_Enum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String sex;
    private String phoneNumber;
    private String dateOfBirth;
    private Role_Enum role;
    private String verificationToken;
    private boolean verified;


}

package com.example.projet2024.mapper;


import com.example.projet2024.DTO.UserDTO;
import com.example.projet2024.entite.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity (UserDTO userDto);
    UserDTO toResponse ( User user );
    List <UserDTO> UserListToUserDTOList ( List <User> userList );

}

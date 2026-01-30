package com.caochung.recruitment.service.mapper;

import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.domain.dto.request.UserRequestDTO;
import com.caochung.recruitment.domain.dto.request.UserUpdateDTO;
import com.caochung.recruitment.domain.dto.response.UserResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRequestDTO userRequestDTO);
    UserResponseDTO toDto(User user);
    List<UserResponseDTO> toDto(List<User> userList);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void fromUpdate(UserUpdateDTO userUpdateDTO, @MappingTarget User user);
}

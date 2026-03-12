package com.caochung.recruitment.config;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.service.UserService;
import com.caochung.recruitment.service.impl.UserServiceImpl;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("userDetailService")
@RequiredArgsConstructor
public class UserDetailCustom implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        if (user.getStatus().equals(UserStatusEnum.DISABLED)) {
            throw new AppException(ErrorCode.USER_DISABLED);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getName())));
    }
}

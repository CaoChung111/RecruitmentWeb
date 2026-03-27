package com.caochung.recruitment.config;

import com.caochung.recruitment.constant.ErrorCode;
import com.caochung.recruitment.constant.UserStatusEnum;
import com.caochung.recruitment.domain.User;
import com.caochung.recruitment.exception.AppException;
import com.caochung.recruitment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("userDetailService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

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
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if(user.getRole() != null && user.getRole().getPermissions() != null) {
            authorities = user.getRole().getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName())).toList();
        }

        return new CustomUserDetails(user.getId(), user.getEmail(), user.getPassword(), user.getName(), user.getRole(), authorities);
    }
}

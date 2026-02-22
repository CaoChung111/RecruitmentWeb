package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.domain.Permission;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {
    @JsonProperty("access_token")
    private String accessToken;
    private UserInfo userInfo;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private RoleResponseDTO role;
    }


    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInsideToken {
        private Long id;
        private String username;
        private String email;
    }
}

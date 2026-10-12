package com.misc.shafagh.cif.impl.dto.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResult {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private String expiresIn;

//    @JsonProperty("refresh_expires_in")
//    private String refreshExpiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

//    @JsonProperty("not-before-policy")
//    private String notBeforePolicy;

//    @JsonProperty("session_state")
//    private String sessionState;

    @JsonProperty("scope")
    private String scope;
}

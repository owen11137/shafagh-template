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
public class LoginRequest {


//    @JsonProperty("grant_type")
//    private String grantType;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}

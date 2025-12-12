package org.phuongnq.analyzer.dto.info;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserInfo user;
}


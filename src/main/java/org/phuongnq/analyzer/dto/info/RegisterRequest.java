package org.phuongnq.analyzer.dto.info;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String shopName;
    private String shopDescription;
}


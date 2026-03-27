package com.hiking.treasure.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String tenantId;
    private String sessionId;
    private List<String> roles;
}

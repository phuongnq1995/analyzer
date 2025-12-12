package org.phuongnq.analyzer.controller;

import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.info.AuthRequest;
import org.phuongnq.analyzer.dto.info.AuthResponse;
import org.phuongnq.analyzer.dto.info.RegisterRequest;
import org.phuongnq.analyzer.dto.info.UserInfo;
import org.phuongnq.analyzer.repository.entity.User;
import org.phuongnq.analyzer.service.UserService;
import org.phuongnq.analyzer.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserInfo> register(@RequestBody RegisterRequest req) {
        User user = userService.register(req);
        return ResponseEntity.ok(new UserInfo(user.getId().toString(), user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        UserDetails ud = userDetailsService.loadUserByUsername(req.getUsername());
        String token = jwtService.generateToken(ud);
        return ResponseEntity.ok(new AuthResponse(token, new UserInfo("", ud.getUsername())));
    }
}

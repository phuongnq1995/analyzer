package org.phuongnq.analyzer.service;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.phuongnq.analyzer.dto.info.RegisterRequest;
import org.phuongnq.analyzer.dto.info.ShopSettings;
import org.phuongnq.analyzer.repository.entity.Role;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.repository.entity.User;
import org.phuongnq.analyzer.repository.RoleRepository;
import org.phuongnq.analyzer.repository.ShopRepository;
import org.phuongnq.analyzer.repository.UserRepository;
import org.phuongnq.analyzer.utils.AuthenticationUtil;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper mapper;

    @Transactional
    public User register(RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        Set<Role> roles = Set.of(userRole);

        User user = User.builder()
            .username(req.getUsername())
            .password(passwordEncoder.encode(req.getPassword()))
            .roles(roles)
            .enabled(true)
            .createdAt(Instant.now())
            .build();

        user = userRepository.save(user);

        Shop shop = Shop.builder()
            .name(req.getShopName())
            .description(req.getShopDescription())
            .user(user)
            .build();

        shopRepository.save(shop);

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), user.isEnabled(), true, true, true, authorities
        );
    }

    @Transactional(readOnly = true)
    public Long getCurrentShopId() {
        return getCurrentShop().getId();
    }

    @Transactional(readOnly = true)
    public Shop getCurrentShop() {
        String username = AuthenticationUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return shopRepository.findByUserId(user.getId())
            .get(0);
    }

    @Transactional(readOnly = true)
    public ShopSettings getCurrentShopSettings() {
        Shop shop = getCurrentShop();
        return mapper.map(shop, ShopSettings.class);
    }

    @Transactional
    public void updateShop(ShopSettings req) {
        Shop shop = getCurrentShop();

        shop.setName(req.getName());
        shop.setDescription(req.getDescription());
        shop.setMarketingFee(BigDecimal.valueOf(req.getMarketingFee()));
        shop.setSalesTax(BigDecimal.valueOf(req.getSalesTax()));

        shopRepository.save(shop);
    }
}


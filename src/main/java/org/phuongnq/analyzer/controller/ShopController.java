package org.phuongnq.analyzer.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.info.ShopSettings;
import org.phuongnq.analyzer.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final UserService userService;

    @GetMapping("/settings")
    public ResponseEntity<ShopSettings> get() {
        return ResponseEntity.ok(userService.getCurrentShopSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<Void> update(@RequestBody ShopSettings req) {
        userService.updateShop(req);
        return ResponseEntity.ok().build();
    }
}

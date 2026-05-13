package com.example.autoshop.users.controller;

import com.example.autoshop.users.dto.ChangePasswordDTO;
import com.example.autoshop.users.dto.InputUserDTO;
import com.example.autoshop.users.dto.UserDTO;
import com.example.autoshop.users.model.PriceLevel;
import com.example.autoshop.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userService.getUserByUsername(authentication.getName())
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(
            Authentication authentication,
            @RequestBody InputUserDTO dto
    ) {

        return ResponseEntity.ok(
                userService.updateOwnProfile(authentication.getName(), dto)
        );
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changeCurrentUserPassword(
            Authentication authentication,
            @RequestBody ChangePasswordDTO dto
    ) {

        userService.changePassword(authentication.getName(), dto);

        return ResponseEntity.ok().build();
    }


    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @RequestBody InputUserDTO dto
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                userService.getAllUsers(page, size)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody InputUserDTO dto
    ) {

        return ResponseEntity.ok(
                userService.updateUser(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordDTO dto
    ) {

        userService.changePassword(id, dto);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/price-levels/all")
    public ResponseEntity<?> getAllPriceLevels() {
        return ResponseEntity.ok(userService.getAllPriceLevels());
    }

        @PostMapping("/price-levels")
        public ResponseEntity<PriceLevel> createPriceLevel(@RequestBody PriceLevel dto) {
                PriceLevel created = userService.createPriceLevel(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }

        @PutMapping("/price-levels/{id}")
        public ResponseEntity<PriceLevel> updatePriceLevel(@PathVariable Long id, @RequestBody PriceLevel dto) {
                return ResponseEntity.ok(userService.updatePriceLevel(id, dto));
        }

        @DeleteMapping("/price-levels/{id}")
        public ResponseEntity<Void> deletePriceLevel(@PathVariable Long id) {
                userService.deletePriceLevel(id);
                return ResponseEntity.noContent().build();
        }
}

package com.example.autoshop.users.service;

import com.example.autoshop.exceptions.WrongPasswordException;
import com.example.autoshop.users.dto.ChangePasswordDTO;
import com.example.autoshop.users.dto.InputUserDTO;
import com.example.autoshop.users.dto.UserDTO;
import com.example.autoshop.users.mapper.UserMapper;
import com.example.autoshop.users.model.PriceLevel;
import com.example.autoshop.users.model.User;
import com.example.autoshop.users.repository.PriceLevelRepository;
import com.example.autoshop.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PriceLevelRepository priceLevelRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public UserDTO createUser(InputUserDTO dto) {

        User user = userMapper.toEntity(dto);

        user.setPasswordHash(
                passwordEncoder.encode(dto.password())
        );

        user.setPriceLevel(
                findPriceLevelById(dto.priceLevelId())
        );

        return userMapper.toDto(
                userRepository.save(user)
        );
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {

        return userRepository.findById(id)
                .filter(User::isActive)
                .map(userMapper::toDto)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {

        return userRepository.findByUsernameAndIsDeletedFalse(username)
                .map(userMapper::toDto)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").ascending()
        );

        return userRepository.findAllByIsDeletedFalse(pageable)
                .map(userMapper::toDto);
    }

    public UserDTO updateUser(
            Long id,
            InputUserDTO dto
    ) {

        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        userMapper.updateUser(dto, user);

        if (dto.password() != null) {
            user.setPasswordHash(
                    passwordEncoder.encode(dto.password())
            );
        }

        if (dto.priceLevelId() != null) {
            user.setPriceLevel(
                    findPriceLevelById(dto.priceLevelId())
            );
        }

        return userMapper.toDto(
                userRepository.save(user)
        );
    }

    public UserDTO updateOwnProfile(
            String username,
            InputUserDTO dto
    ) {

        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (dto.username() != null && !dto.username().isBlank()) {
            user.setUsername(dto.username());
        }

        if (dto.email() != null && !dto.email().isBlank()) {
            user.setEmail(dto.email());
        }

        user.setDeliveryAddress(dto.deliveryAddress());

        if (dto.companyName() != null && !dto.companyName().isBlank()) {
            user.setCompanyName(dto.companyName());
        }

        if (dto.phone() != null && !dto.phone().isBlank()) {
            user.setPhone(dto.phone());
        }

        return userMapper.toDto(
                userRepository.save(user)
        );
    }

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.markDeleted();
        userRepository.save(user);
    }

    private PriceLevel findPriceLevelById(Long id) {

        return priceLevelRepository.findById(id)
                .filter(PriceLevel::isActive)
                .orElseThrow(() ->
                        new RuntimeException("Price level not found")
                );
    }

    public void changePassword(
            Long userId,
            @NonNull ChangePasswordDTO dto
    ) {

        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (!passwordEncoder.matches(
                dto.oldPassword(),
                user.getPasswordHash()
        )) {
            throw new WrongPasswordException(
                    "Wrong old password"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(dto.newPassword())
        );

        userRepository.save(user);
    }

    public void changePassword(
            String username,
            @NonNull ChangePasswordDTO dto
    ) {

        User user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        changePassword(user.getId(), dto);
    }

    @Transactional(readOnly = true)
    public java.util.List<PriceLevel> getAllPriceLevels() {
        return priceLevelRepository.findAllByIsDeletedFalse();
    }

        public PriceLevel createPriceLevel(PriceLevel dto) {
                PriceLevel pl = new PriceLevel();
                pl.setName(dto.getName());
                pl.setRatio(dto.getRatio());
                return priceLevelRepository.save(pl);
        }

        public PriceLevel updatePriceLevel(Long id, PriceLevel dto) {
                PriceLevel existing = findPriceLevelById(id);
                existing.setName(dto.getName());
                existing.setRatio(dto.getRatio());
                return priceLevelRepository.save(existing);
        }

        public void deletePriceLevel(Long id) {
                PriceLevel priceLevel = findPriceLevelById(id);
                priceLevel.markDeleted();
                priceLevelRepository.save(priceLevel);
        }
}

package com.example.autoshop.users.mapper;

import com.example.autoshop.users.dto.InputUserDTO;
import com.example.autoshop.users.dto.UserDTO;
import com.example.autoshop.users.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy =
                NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "priceLevel", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(InputUserDTO dto);

    @Mapping(source = "priceLevel.id", target = "priceLevelId")
    @Mapping(source = "priceLevel.name", target = "priceLevelName")
    @Mapping(source = "priceLevel.ratio", target = "priceLevelRatio")
    UserDTO toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "priceLevel", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateUser(
            InputUserDTO dto,
            @MappingTarget User user
    );
}

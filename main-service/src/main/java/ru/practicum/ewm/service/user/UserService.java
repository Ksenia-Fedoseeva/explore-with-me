package ru.practicum.ewm.service.user;

import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest request);

    void deleteUser(Long userId);

    List<UserDto> getUsers(List<Long> ids, int from, int size);
}

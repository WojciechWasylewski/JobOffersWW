package com.joboffers.domain.loginandregister;

import com.joboffers.domain.loginandregister.dto.NewUserDto;
import com.joboffers.domain.loginandregister.dto.RegistrationResultDto;
import com.joboffers.domain.loginandregister.dto.UserDto;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginAndRegisterFacade {
    private static final String USER_NOT_FOUND = "User not found";
    private final UserRepository userRepository;

    public UserDto findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserDto(user.id(), user.password(), user.username()))
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }

    public RegistrationResultDto register(NewUserDto newUserDto) {
        final User user = User.builder()
                .username(newUserDto.username())
                .password(newUserDto.password())
                .build();
        User savedUser = userRepository.save(user);
        return new RegistrationResultDto(savedUser.id(), true, savedUser.username());
    }
}

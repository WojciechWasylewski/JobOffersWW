package com.joboffers.domain.loginandregister;

import com.joboffers.domain.loginandregister.dto.NewUserDto;
import com.joboffers.domain.loginandregister.dto.RegistrationResultDto;
import com.joboffers.domain.loginandregister.dto.UserDto;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

class LoginAndRegisterFacadeTest {
    LoginAndRegisterFacade loginFacade = new LoginAndRegisterFacade(
            new InMemoryLoginRepository()
    );

    @Test
    public void should_find_user_by_user_name() {
        //given
        NewUserDto newUserDto = new NewUserDto("username", "password");
        RegistrationResultDto register = loginFacade.register(newUserDto);

        //when
        UserDto byUsername = loginFacade.findByUsername(register.username());

        //then
        assertThat(byUsername).isEqualTo(new UserDto(register.id(), "password", "username"));
    }

    @Test
    public void should_throw_exception_when_user_not_found() {
        //given
        String username = "someUser";

        //when
        Throwable thrown = catchThrowable(() -> loginFacade.findByUsername(username));

        //then
        AssertionsForClassTypes.assertThat(thrown)
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    public void should_register_user() {
        //given
        NewUserDto newUserDto = new NewUserDto("username", "password");

        //when
        RegistrationResultDto register = loginFacade.register(newUserDto);

        //then
        assertAll(
                () -> assertThat(register.created()).isTrue(),
                () -> assertThat(register.username()).isEqualTo("username")
        );
    }
}
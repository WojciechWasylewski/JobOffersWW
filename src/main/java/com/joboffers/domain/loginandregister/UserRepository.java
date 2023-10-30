package com.joboffers.domain.loginandregister;


import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findByUsername(String userName);

}

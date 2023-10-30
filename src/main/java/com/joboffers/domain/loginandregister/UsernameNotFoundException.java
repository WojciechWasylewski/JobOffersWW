package com.joboffers.domain.loginandregister;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String userNotFound) {
        super(userNotFound);
    }
}

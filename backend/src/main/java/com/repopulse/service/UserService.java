package com.repopulse.service;

import com.repopulse.entity.User;
import com.repopulse.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveOrGetUser(Map<String, Object> githubUser) {

        Long githubId = ((Number) githubUser.get("id")).longValue();
        String username = (String) githubUser.get("login");

        return userRepository.findByGithubId(githubId)
                .orElseGet(() -> {

                    User user = new User();

                    user.setGithubId(githubId);
                    user.setUsername(username);
                    user.setCreatedAt(Instant.now());

                    return userRepository.save(user);
                });
    }
}
package com.repopulse.repository;

import com.repopulse.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/repopulse",
        "spring.datasource.username=repopulse",
        "spring.datasource.password=repopulse_dev"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByGithubId() {

        User user = new User();
        user.setGithubId(123456789L);
        user.setUsername("test-user");
        user.setCreatedAt(Instant.now());

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();

        Optional<User> foundUser =
                userRepository.findByGithubId(123456789L);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername())
                .isEqualTo("test-user");
    }
}
package com.mytodolist.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mytodolist.model.User;
import com.mytodolist.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserService userService;

    @Test
    public void testCreateUser() {
    }

    @Test
    public void testFindByUsername() {
        User mockUser = new User("testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        Optional<User> foundUser = userService.findByUsername("testuser");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testFindById() {
        User mockUser = new User("testuser", "password");
        mockUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        Optional<User> foundUser = userService.findById(1L);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(1L);
    }

    @Test
    public void testDeleteUser() {
    }

}

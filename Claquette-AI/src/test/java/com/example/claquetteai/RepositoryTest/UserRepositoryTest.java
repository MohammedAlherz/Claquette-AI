package com.example.claquetteai.RepositoryTest;

import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    UserRepository userRepository;

    @Test
    void findUserById_returnsStubbedUser() {
        User stub = validUser(1, "Hussam", "hussam@example.com");
        when(userRepository.findUserById(1)).thenReturn(stub);

        User found = userRepository.findUserById(1);

        Assertions.assertNotNull(found);
        Assertions.assertEquals(1, found.getId());
        Assertions.assertEquals("Hussam", found.getFullName());
        Assertions.assertEquals("hussam@example.com", found.getEmail());
        verify(userRepository, times(1)).findUserById(1);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findUserById_returnsNull_whenMissing() {
        when(userRepository.findUserById(999)).thenReturn(null);
        Assertions.assertNull(userRepository.findUserById(999));
        verify(userRepository).findUserById(999);
        verifyNoMoreInteractions(userRepository);
    }

    private static User validUser(Integer id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setFullName(name);
        u.setEmail(email);
        u.setPassword("Abcdef1@");
        u.setRole("COMPANY");
        return u;
    }
}

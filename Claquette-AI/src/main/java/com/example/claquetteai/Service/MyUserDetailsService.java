package com.example.claquetteai.Service;


import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository authRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = authRepository.findUserByEmail(email);

        if (user == null){
            throw new ApiException("Wrong email or Password!");
        }

        return user;
    }
}
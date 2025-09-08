package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final Map<String, String> codes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final UserRepository userRepository;

    public String generateCode(String email) {
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        codes.put(email, code);

        scheduler.schedule(() -> codes.remove(email), 10, TimeUnit.MINUTES);

        return code;
    }

    public boolean verifyCode(Integer userId, String inputCode) {
        User user = userRepository.findUserById(userId);
        if(user == null){
            throw new ApiException("User not found");
        }
        String storedCode = codes.get(user.getEmail());
        if (storedCode != null && storedCode.equals(inputCode)) {
            codes.remove(user.getEmail());
            return true;
        }
        return false;
    }
}

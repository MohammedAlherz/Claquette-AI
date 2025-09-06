package com.example.claquetteai.Service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationService {
    private final Map<String, String> codes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public String generateCode(String email) {
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        codes.put(email, code);

        scheduler.schedule(() -> codes.remove(email), 10, TimeUnit.MINUTES);

        return code;
    }

    public boolean verifyCode(String email, String inputCode) {
        String storedCode = codes.get(email);
        if (storedCode != null && storedCode.equals(inputCode)) {
            codes.remove(email);
            return true;
        }
        return false;
    }
}

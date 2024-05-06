package com.carcompany.services.token;

import com.carcompany.models.Token;
import com.carcompany.models.User;
import org.springframework.stereotype.Service;

@Service
public interface ITokenService {
    Token addToken(User user, String token, boolean isMobileDevice);
    Token refreshToken(String refreshToken, User user) throws Exception;
}
package br.com.mechanic.account.service.auth;

import br.com.mechanic.account.config.security.AuthJwtProperties;
import br.com.mechanic.account.constant.AuthValidationConstants;
import br.com.mechanic.account.constant.AuthTokenConstants;
import br.com.mechanic.account.constant.SecurityAuthorityConstants;
import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.enuns.AccountStatusEnum;
import br.com.mechanic.account.enuns.TopicStatusEnum;
import br.com.mechanic.account.repository.account.AccountProfileRepository;
import br.com.mechanic.account.repository.account.jpa.AccountRepositoryJpa;
import br.com.mechanic.account.repository.account.jpa.TopicRepositoryJpa;
import br.com.mechanic.account.security.TokenService;
import br.com.mechanic.account.service.request.LoginRequest;
import br.com.mechanic.account.service.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceBO {

    private final AccountRepositoryJpa accountRepositoryJpa;
    private final AccountProfileRepository accountProfileRepository;
    private final TopicRepositoryJpa topicRepositoryJpa;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthJwtProperties authJwtProperties;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        Account account = accountRepositoryJpa.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException(AuthValidationConstants.MESSAGE_INVALID_CREDENTIALS));

        if (account.getStatus() != AccountStatusEnum.ACTIVE) {
            throw new BadCredentialsException(AuthValidationConstants.MESSAGE_INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new BadCredentialsException(AuthValidationConstants.MESSAGE_INVALID_CREDENTIALS);
        }

        long accountId = account.getId();
        long profileCount = accountProfileRepository.countByAccount_Id(accountId);
        long openTopicCount = topicRepositoryJpa.countByAccount_IdAndStatus(accountId, TopicStatusEnum.OPEN);

        List<String> authorities = buildAuthorities(profileCount, openTopicCount);
        String token = tokenService.issueAccessToken(accountId, authorities);

        return new LoginResponse(
                token,
                AuthTokenConstants.BEARER_TOKEN_TYPE,
                authJwtProperties.getExpirationSeconds(),
                List.copyOf(authorities)
        );
    }

    private static List<String> buildAuthorities(long profileLinkCount, long openTopicCount) {
        List<String> authorities = new ArrayList<>();
        authorities.add(SecurityAuthorityConstants.ANNOTATOR);
        if (openTopicCount > 0) {
            authorities.add(SecurityAuthorityConstants.OWNER_FULL);
            return authorities;
        }
        if (profileLinkCount > 1) {
            authorities.add(SecurityAuthorityConstants.OWNER_STANDARD);
        }
        return authorities;
    }
}

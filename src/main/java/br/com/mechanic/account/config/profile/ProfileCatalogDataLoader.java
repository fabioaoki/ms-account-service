package br.com.mechanic.account.config.profile;

import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.entity.profile.Profile;
import br.com.mechanic.account.repository.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ProfileCatalogDataLoader implements ApplicationRunner {

    private final ProfileRepository profileRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (profileRepository.count() > 0) {
            return;
        }
        LocalDateTime createdAt = LocalDateTime.now();
        for (AccountProfileTypeEnum profileType : AccountProfileTypeEnum.values()) {
            profileRepository.save(Profile.builder()
                    .profileType(profileType)
                    .createdAt(createdAt)
                    .build());
        }
    }
}

package br.com.mechanic.account.mapper.account;

import br.com.mechanic.account.entity.account.Account;
import br.com.mechanic.account.entity.account.AccountHistory;
import br.com.mechanic.account.entity.account.AccountProfile;
import br.com.mechanic.account.entity.profile.Profile;
import br.com.mechanic.account.enuns.AccountHistoryActionEnum;
import br.com.mechanic.account.enuns.AccountProfileTypeEnum;
import br.com.mechanic.account.model.account.AccountModel;
import br.com.mechanic.account.service.request.UserCreateRequest;
import br.com.mechanic.account.service.response.AccountProfileLinkResponse;
import br.com.mechanic.account.service.response.AccountProfileUnlinkResponse;
import br.com.mechanic.account.service.response.AccountResponse;
import br.com.mechanic.account.service.response.AccountUpdateResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mapper {

    public static AccountModel toModel(
            UserCreateRequest request,
            String normalizedEmail,
            String formattedName,
            PasswordEncoder passwordEncoder
    ) {
        return AccountModel.builder()
                .email(normalizedEmail)
                .name(formattedName)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .birthDate(request.birthDate())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
    }

    public static Account toEntity(AccountModel model) {
        return Account.builder()
                .email(model.getEmail())
                .name(model.getName())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .birthDate(model.getBirthDate())
                .passwordHash(model.getPasswordHash())
                .build();
    }

    public static AccountResponse toResponse(Account account, AccountProfileTypeEnum profileTypeForApi) {
        return AccountResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .name(account.getName())
                .birthDate(account.getBirthDate())
                .profileType(profileTypeForApi)
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    public static AccountProfile toAccountProfile(Account account, Profile profile) {
        return AccountProfile.builder()
                .account(account)
                .profile(profile)
                .build();
    }

    public static AccountHistory toAccountHistory(
            Account account,
            Profile profile,
            AccountHistoryActionEnum action
    ) {
        return AccountHistory.builder()
                .account(account)
                .profile(profile)
                .action(action)
                .build();
    }

    public static AccountUpdateResponse toUpdateResponse(Account account) {
        return AccountUpdateResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .birthDate(account.getBirthDate())
                .lastUpdatedAt(account.getLastUpdatedAt())
                .build();
    }

    public static AccountProfileLinkResponse toProfileLinkResponse(Account account, Profile profile) {
        return AccountProfileLinkResponse.builder()
                .accountId(account.getId())
                .profileId(profile.getId())
                .profileType(profile.getProfileType())
                .build();
    }

    public static AccountProfileUnlinkResponse toProfileUnlinkResponse(Account account, Profile profile) {
        return AccountProfileUnlinkResponse.builder()
                .accountId(account.getId())
                .profileId(profile.getId())
                .profileType(profile.getProfileType())
                .build();
    }
}

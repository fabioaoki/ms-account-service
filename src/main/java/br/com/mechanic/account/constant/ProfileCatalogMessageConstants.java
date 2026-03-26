package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProfileCatalogMessageConstants {

    public static final String REQUIRED_PROFILE_MISSING_FROM_CATALOG =
            "Required profile is missing from catalog; verify Profile seed data.";
}

package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountPresentationSummaryValidationConstants {

    public static final int MIN_SUMMARY_CHAR_COUNT_AFTER_TRIM = 2;
    public static final int MAX_SUMMARY_CHAR_COUNT = EntityConstants.ACCOUNT_PRESENTATION_SUMMARY_TEXT_COLUMN_LENGTH;

    public static final String MESSAGE_SUMMARY_REQUIRED = "summary is required.";
    public static final String MESSAGE_SUMMARY_INVALID_LENGTH =
            "summary must contain at least %d characters after trimming.";
    public static final String MESSAGE_SUMMARY_EXCEEDS_MAX_LENGTH =
            "summary exceeds maximum length (%d characters).";
    public static final String MESSAGE_ACCOUNT_MUST_HAVE_MORE_THAN_ONE_PROFILE_TYPE =
            "Only accounts with more than one profile type can create or update presentation summary.";
    public static final String MESSAGE_ACCOUNT_PRESENTATION_SUMMARY_ALREADY_EXISTS =
            "Presentation summary already exists for this account.";
    public static final String MESSAGE_ACCOUNT_PRESENTATION_SUMMARY_NOT_FOUND =
            "Presentation summary not found for this account.";
}

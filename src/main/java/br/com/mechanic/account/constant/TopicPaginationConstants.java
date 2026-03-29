package br.com.mechanic.account.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicPaginationConstants {

    public static final int DEFAULT_PAGE_NUMBER = 0;

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int MIN_PAGE_NUMBER = 0;

    public static final int MIN_PAGE_SIZE = 1;

    public static final int MAX_PAGE_SIZE = 100;

    public static final String SORT_PROPERTY_CREATED_AT = "createdAt";

    public static final String SORT_PROPERTY_ID = "id";
}

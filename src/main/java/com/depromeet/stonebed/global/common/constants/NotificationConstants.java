package com.depromeet.stonebed.global.common.constants;

import java.time.format.DateTimeFormatter;

public final class NotificationConstants {
    public static final int SQS_BATCH_SIZE = 10;
    public static final long FIRST_BOOST_THRESHOLD = 1;
    public static final long POPULAR_THRESHOLD = 1000;
    public static final long SUPER_POPULAR_THRESHOLD = 5000;
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
}

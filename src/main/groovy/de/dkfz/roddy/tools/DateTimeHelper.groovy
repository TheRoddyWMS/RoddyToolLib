/*
 * Copyright (c) 2024 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools

import groovy.transform.CompileStatic

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@CompileStatic
class DateTimeHelper {

    private final DateTimeFormatter DATE_PATTERN

    DateTimeHelper(String dateParserPattern = null, Locale locale = Locale.default, ZoneId timeZoneId = ZoneId.systemDefault()) {
        if (!dateParserPattern)
            DATE_PATTERN = DateTimeFormatter.ISO_DATE_TIME
                    .withLocale(locale)
                    .withZone(timeZoneId ?: null)
        else
            DATE_PATTERN = DateTimeFormatter
                    .ofPattern(dateParserPattern)
                    .withLocale(locale)
                    .withZone(timeZoneId ?: null)
    }

    DateTimeFormatter getDatePattern() {
        return DATE_PATTERN
    }

    ZonedDateTime parseToZonedDateTime(String str) {
        ZonedDateTime date = ZonedDateTime.parse(str, DATE_PATTERN)
        return date
    }

    static Duration differenceBetween(LocalDateTime a, LocalDateTime b) {
        Duration result = Duration.between(a, b)
        if (result.isNegative())
            result = result.multipliedBy(-1)
        result
    }

    static boolean durationExceeds(Duration duration, Duration maximum) {
        (maximum - duration).isNegative()
    }
}

/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.clienthints.parsers;

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.ClientHintsCacheInstantiator;
import nl.basjes.parse.useragent.clienthints.ClientHints;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public interface CHParser extends Serializable {
    /**
     * Parses the provided Client Hints request Headers and updates the provided ClientHints instance
     *
     * @param clientHintsHeaders The map of request headers that at least contains one of the supported fields
     * @param clientHints        The instance that is to be updated
     * @param headerName         The actual name of the header (must do case-insensitive compare!)
     * @return The same instance as the provided clientHints parameter.
     */
    @Nonnull
    ClientHints parse(@Nonnull Map<String, String> clientHintsHeaders, @Nonnull ClientHints clientHints, @Nonnull String headerName);

    /**
     * What Client Hint can this parser do something with?
     *
     * @return The nonnull header name this parser can handle.
     */
    @Nonnull
    String inputField();

    /**
     * Determines if the provided value is a sf-boolean.
     *
     * @param value The value to be parsed
     * @return True/False or null if this was NOT a boolean.
     */
    default Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }
        switch (value) {
            case "?0" : return FALSE;
            case "?1" : return TRUE;
            default   : return null;
        }
    }

    Pattern SF_STRING_PATTERN = Pattern.compile("^\"(.*)\"$");

    /**
     * A sf-string is a String with '"' around it.
     *
     * @param value The value to be parsed
     * @return The actual payload string (i.e. without the surrounding '"') or null if invalid
     */
    default String parseSfString(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = SF_STRING_PATTERN.matcher(value);
        if (matcher.find()) {
            String result = matcher.group(1).trim();
            if (result.isEmpty()) {
                return null;
            }
            return result;
        }
        return null;
    }

    /**
     * A sf-list is a ',' separated list of sf-string.
     *
     * @param value The value to be parsed
     * @return The list of actual payload strings (i.e. without the surrounding '"') or null if invalid
     */
    default ArrayList<String> parseSfList(String value) {
        if (value == null) {
            return null;
        }

        return Arrays
            .stream(value.split(","))
            .map(String::trim)
            .map(this::parseSfString)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    default void initializeCache(@Nonnull ClientHintsCacheInstantiator<?> clientHintsCacheInstantiator, int cacheSize) {
        // No cache implemented by default.
    }

    default void clearCache() {
        // No cache implemented by default.
    }

}

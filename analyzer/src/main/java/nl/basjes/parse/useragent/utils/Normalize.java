/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2024 Niels Basjes
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

package nl.basjes.parse.useragent.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Normalize {
    private Normalize() {}

    private static boolean isTokenSeparator(char letter) {
        switch (letter) {
            case ' ':
            case '-':
            case '_':
            case '/':
                return true;
            default:
                return false;
        }
    }

    public static String brand(String brand) {
        if (brand == null) {
            return null;
        }
        if (brand.length() <= 3) {
            return brand.toUpperCase(Locale.ENGLISH);
        }

        StringBuilder sb = new StringBuilder(brand.length());
        char[] nameChars = brand.toCharArray();

        StringBuilder wordBuilder = new StringBuilder(brand.length());

        int lowerChars = 0;
        boolean wordHasNumbers = false;
        for (int i = 0; i < nameChars.length; i++) {
            char thisChar = nameChars[i];
            if (Character.isDigit(thisChar)) {
                wordHasNumbers = true;
            }

            if (isTokenSeparator(thisChar)) {
                if (wordBuilder.length() <= 3 || wordHasNumbers) {
                    sb.append(wordBuilder.toString().toUpperCase(Locale.ENGLISH));
                } else {
                    sb.append(wordBuilder);
                }
                wordBuilder.setLength(0);
                lowerChars = 0; // Next word
                wordHasNumbers = false;
                sb.append(thisChar);
            } else {
                if (wordBuilder.length() == 0) { // First letter of a word
                    wordBuilder.append(Character.toUpperCase(thisChar));
                } else {
                    boolean isUpperCase = Character.isUpperCase(thisChar);

                    if (isUpperCase) {
                        if (lowerChars >= 3) {
                            wordBuilder.append(thisChar);
                        } else {
                            wordBuilder.append(Character.toLowerCase(thisChar));
                        }
                        lowerChars = 0;
                    } else {
                        wordBuilder.append(Character.toLowerCase(thisChar));
                        lowerChars++;
                    }
                }
                // This was the last letter?
                if (i == (nameChars.length-1)) {
                    if (wordBuilder.length() <= 3 || wordHasNumbers) {
                        sb.append(wordBuilder.toString().toUpperCase(Locale.ENGLISH));
                    } else {
                        sb.append(wordBuilder);
                    }
                    wordBuilder.setLength(0);
                    lowerChars = 0; // Next word
                    wordHasNumbers = false;
                }
            }
        }
        return sb.toString().trim();
    }

    private static final Pattern DEVICE_CLEANUP_PATTERN_1 = Pattern.compile("- +");
    private static final Pattern DEVICE_CLEANUP_PATTERN_2 = Pattern.compile(" +-");
    private static final Pattern DEVICE_CLEANUP_PATTERN_3 = Pattern.compile(" +");
    private static final Pattern DEVICE_CLEANUP_PATTERN_4 = Pattern.compile("( -| )+");

    private static final String MOZILLA = "Mozilla";
    private static final int    MOZILLA_LENGTH = MOZILLA.length();

    public static String cleanupDeviceBrandName(String deviceBrand, String deviceName) {
        deviceName = replaceString(deviceName, "'", " ");
        deviceName = replaceString(deviceName, "_", " ");

        deviceName = DEVICE_CLEANUP_PATTERN_1.matcher(deviceName).replaceAll("-");
        deviceName = DEVICE_CLEANUP_PATTERN_2.matcher(deviceName).replaceAll("-");
        deviceName = DEVICE_CLEANUP_PATTERN_3.matcher(deviceName).replaceAll(" ");

        String lowerDeviceName = deviceName.toLowerCase(Locale.ENGLISH);

        if (!deviceBrand.isEmpty()) {
            String lowerDeviceBrand = deviceBrand.toLowerCase(Locale.ENGLISH);

            // In some cases it does start with the brand but without a separator following the brand
            if (lowerDeviceName.startsWith(lowerDeviceBrand)) {
                deviceName = replaceString(deviceName, "_", " ");
                // (?i) means: case insensitive
                deviceName = deviceName.replaceAll("(?i)^" + Pattern.quote(deviceBrand) + "([^ ].*)$", Matcher.quoteReplacement(deviceBrand) + " $1");
                deviceName = DEVICE_CLEANUP_PATTERN_4.matcher(deviceName).replaceAll(" ");
            } else {
                deviceName = deviceBrand + ' ' + deviceName;
            }
        }
        String result = Normalize.brand(deviceName);

        if (result.indexOf('I') != -1) {
            result = replaceString(result, "Ipad", "iPad");
            result = replaceString(result, "Ipod", "iPod");
            result = replaceString(result, "Iphone", "iPhone");
            result = replaceString(result, "IOS ", "iOS ");
        }

        if (result.length() > MOZILLA_LENGTH && result.endsWith(MOZILLA)) {
            int newLength = result.length() - MOZILLA.length();
            if (result.charAt(newLength-1) == '-') {
                newLength--;
            }
            while (result.charAt(newLength-1) == ' ') {
                newLength--;
            }
            result = result.substring(0, newLength);
        }

        return result;
    }

    public static String email(String email) {
        String cleaned = email;
        cleaned = replaceString(cleaned, "[at]", "@");
        cleaned = replaceString(cleaned, "[\\xc3\\xa07]", "@");
        cleaned = replaceString(cleaned, "[dot]", ".");
        cleaned = replaceString(cleaned, "\\", " ");
        cleaned = replaceString(cleaned, " at ", "@");
        cleaned = replaceString(cleaned, "dot", ".");
        cleaned = replaceString(cleaned, " dash ", "-");
        cleaned = replaceString(cleaned, " ", "");
        return cleaned;
    }

    public static String replaceString(
        final String input,
        final String searchFor,
        final String replaceWith
    ){
        // Avoid infinite loop problem
        if (input.isEmpty() || searchFor.isEmpty()) {
            return input;
        }

        // Speedup
        if (searchFor.equals(replaceWith)) {
            return input;
        }

        //startIdx and idxSearchFor delimit various chunks of input; these
        //chunks always end where searchFor begins
        int startIdx = 0;
        int idxSearchFor = input.indexOf(searchFor, startIdx);
        if (idxSearchFor < 0) {
            return input;
        }
        final StringBuilder result = new StringBuilder(input.length()+32);

        while (idxSearchFor >= 0) {
            //grab a part of input which does not include searchFor
            result.append(input, startIdx, idxSearchFor);
            //add replaceWith to take place of searchFor
            result.append(replaceWith);

            //reset the startIdx to just after the current match, to see
            //if there are any further matches
            startIdx = idxSearchFor + searchFor.length();
            idxSearchFor = input.indexOf(searchFor, startIdx);
        }
        //the final chunk will go to the end of input
        result.append(input.substring(startIdx));
        return result.toString();
    }

    public static boolean isLowerCase(String text) {
        return text
            .codePoints()
            .noneMatch(Character::isUpperCase); // Using the uppercase check because numbers are not lowercase.
    }

}


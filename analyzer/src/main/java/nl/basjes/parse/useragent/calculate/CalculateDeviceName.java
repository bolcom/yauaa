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

package nl.basjes.parse.useragent.calculate;

import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.utils.Normalize;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.LITERAL;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VALUE;

public class CalculateDeviceName extends FieldCalculator {


    private static final Pattern CLEAN_1_PATTERN = Pattern.compile("AppleWebKit", CASE_INSENSITIVE | LITERAL);

    private String removeBadSubStrings(String input) {
        input = CLEAN_1_PATTERN.matcher(input).replaceAll("");
        return input;
    }

    @Override
    public void calculate(MutableUserAgent userAgent) {
        // Make sure the DeviceName always starts with the DeviceBrand
        AgentField deviceName = userAgent.get(DEVICE_NAME);
        if (!deviceName.isDefaultValue()) {
            AgentField deviceBrand = userAgent.get(DEVICE_BRAND);
            String deviceNameValue = removeBadSubStrings(deviceName.getValue());
            String deviceBrandValue = deviceBrand.getValue();
            if (deviceName.getConfidence() >= 0 &&
                deviceBrand.getConfidence() >= 0 &&
                !deviceBrandValue.equals(UNKNOWN_VALUE)) {
                // In some cases it does start with the brand but without a separator following the brand
                deviceNameValue = Normalize.cleanupDeviceBrandName(deviceBrandValue, deviceNameValue);
            } else {
                deviceNameValue = Normalize.brand(deviceNameValue);
            }

            userAgent.setForced(
                DEVICE_NAME,
                deviceNameValue,
                deviceName.getConfidence());
        }
    }

    @Override
    public String getCalculatedFieldName() {
        return DEVICE_NAME;
    }

    @Override
    public Set<String> getDependencies() {
        return Collections.singleton(DEVICE_BRAND);
    }

    @Override
    public String toString() {
        return "Calculate " + DEVICE_NAME;
    }

}

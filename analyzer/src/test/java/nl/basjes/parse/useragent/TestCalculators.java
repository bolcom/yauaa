/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2025 Niels Basjes
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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.calculate.CalculateAgentName;
import nl.basjes.parse.useragent.calculate.CalculateDeviceBrand;
import nl.basjes.parse.useragent.calculate.CalculateDeviceName;
import nl.basjes.parse.useragent.calculate.ConcatNONDuplicatedCalculator;
import nl.basjes.parse.useragent.calculate.MajorVersionCalculator;
import nl.basjes.parse.useragent.config.AnalyzerConfig;
import nl.basjes.parse.useragent.config.AnalyzerConfigHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCalculators {

    private static final Logger LOG = LogManager.getLogger(TestCalculators.class);

    private static final AnalyzerConfigHolder CONFIG_HOLDER = new AnalyzerConfigHolder() {
        @Nonnull
        @Override
        public AnalyzerConfig getConfig() {
            Map<String, String> mobileBrandPrefixes = new TreeMap<>();
            mobileBrandPrefixes.put("SM-", "Samsung");
            Map<String, String> mobileBrands = new TreeMap<>();
            mobileBrands.put("Nokia", "Nokia");
            mobileBrands.put("Samsung", "Samsung");

            return AnalyzerConfig
                .newBuilder()
                .putLookup("MobileBrandPrefixes", mobileBrandPrefixes)
                .putLookup("MobileBrands", mobileBrands)
                .build();
        }
    };

    @Test
    void testFieldAgentNameVersionFallback() {
        MutableUserAgent userAgent = new MutableUserAgent();
        userAgent.setForced(DEVICE_BRAND, "some_thing", 1);
        userAgent.setForced(AGENT_VERSION, "1.2.3", 1);

        new CalculateDeviceBrand(CONFIG_HOLDER).calculate(userAgent);
        new CalculateAgentName().calculate(userAgent);

        new MajorVersionCalculator(AGENT_VERSION_MAJOR, AGENT_VERSION).calculate(userAgent);
        new ConcatNONDuplicatedCalculator(AGENT_NAME_VERSION, AGENT_NAME, AGENT_VERSION).calculate(userAgent);
        new ConcatNONDuplicatedCalculator(AGENT_NAME_VERSION_MAJOR, AGENT_NAME, AGENT_VERSION_MAJOR).calculate(userAgent);

        assertEquals("Some_Thing", userAgent.getValue(DEVICE_BRAND));
        assertEquals("Some_Thing", userAgent.getValue(AGENT_NAME));
        assertEquals("Some_Thing 1.2.3", userAgent.getValue(AGENT_NAME_VERSION));
        assertEquals("1", userAgent.getValue(AGENT_VERSION_MAJOR));
        assertEquals("Some_Thing 1", userAgent.getValue(AGENT_NAME_VERSION_MAJOR));
    }

    void assertMajorVersion(String input, String expectedOutput) {
        MutableUserAgent userAgent = new MutableUserAgent();
        userAgent.setForced("Dummy", input, 1);

        new MajorVersionCalculator("Result", "Dummy").calculate(userAgent);
        assertEquals(expectedOutput, userAgent.getValue("Result"));
    }

    @Test
    void testMajorVersionCalulation() {
        assertMajorVersion("Basjes 1.2.3", "Basjes 1");
        assertMajorVersion("Windows NT 10.0", "Windows NT 10");
        assertMajorVersion("Windows-NT 10.0", "Windows-NT 10");
        assertMajorVersion("Windows_NT 10.0", "Windows_NT 10");
    }

    private static final class UrlBrandPair {
        final String url;
        final String brand;

        UrlBrandPair(String url, String brand) {
            this.url = url;
            this.brand = brand;
        }

        @Override
        public String toString() {
            return url + " --> " + brand;
        }
    }

    public static Iterable<UrlBrandPair> urlsAndBrands() {
        List<UrlBrandPair> list = new ArrayList<>();
        list.add(new UrlBrandPair("https://yauaa.basjes.nl",                    "Basjes"));
        list.add(new UrlBrandPair("https://about.censys.io/",                   "Censys"));
        list.add(new UrlBrandPair("http://www.google.com/bot.html",             "Google"));
        list.add(new UrlBrandPair("https://github.com/",                        "Unknown"));
        list.add(new UrlBrandPair("https://github.com/nielsbasjes/yauaa",       "Nielsbasjes"));
        list.add(new UrlBrandPair("https://gitlab.com/niels/yauaa",             "Niels"));
        list.add(new UrlBrandPair("https://github.com/acmesh-official/acme.sh", "Acmesh-Official"));

        // Blacklisted
        list.add(new UrlBrandPair("http://localhost", "Unknown"));
        list.add(new UrlBrandPair("http://bit.ly/",   "Unknown"));
        list.add(new UrlBrandPair("https://bit.ly/",  "Unknown"));

        // Not a domain
        list.add(new UrlBrandPair("http://50.22.201.16/Wazzup", "Unknown"));

        list.add(new UrlBrandPair("http://amzn.to/1vsZADi", "Amzn"));

        return list;
    }

    @ParameterizedTest(name = "Test {index} -> Input: \"{0}\"")
    @MethodSource("urlsAndBrands")
    void checkBrandUrlExtraction(UrlBrandPair pair) {
        LOG.info("URL: {}", pair);
        MutableUserAgent userAgent = new MutableUserAgent();
        userAgent.setForced(UserAgent.AGENT_INFORMATION_URL, pair.url, 1);
        new CalculateDeviceBrand(CONFIG_HOLDER).calculate(userAgent);
        assertEquals(pair.brand, userAgent.getValue(DEVICE_BRAND));
    }

    @Test
    void checkBrandLookup1() {
        MutableUserAgent userAgent = new MutableUserAgent();
        userAgent.setForced(DEVICE_NAME, "SM-some_thing", 1);

        new CalculateDeviceBrand(CONFIG_HOLDER).calculate(userAgent);
        new CalculateDeviceName().calculate(userAgent);

        assertEquals("Samsung", userAgent.getValue(DEVICE_BRAND));
        assertEquals("Samsung SM-Some Thing", userAgent.getValue(DEVICE_NAME));
    }

    @Test
    void checkBrandLookup2() {
        MutableUserAgent userAgent = new MutableUserAgent();
        userAgent.setForced(DEVICE_NAME, "Nokiasome_thing", 1);

        new CalculateDeviceBrand(CONFIG_HOLDER).calculate(userAgent);
        new CalculateDeviceName().calculate(userAgent);

        assertEquals("Nokia", userAgent.getValue(DEVICE_BRAND));
        assertEquals("Nokia Some Thing", userAgent.getValue(DEVICE_NAME));
    }

}

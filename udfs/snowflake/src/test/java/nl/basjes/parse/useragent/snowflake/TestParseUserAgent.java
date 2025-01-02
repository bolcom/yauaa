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

package nl.basjes.parse.useragent.snowflake;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestParseUserAgent {

    @Test
    void testBasic() {
        // This is an edge case where the webview fields are calculated AND wiped again.
        String userAgent = "Mozilla/5.0 (Linux; Android 5.1.1; KFFOWI Build/LMY47O) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Version/4.0 Chrome/41.51020.2250.0246 Mobile Safari/537.36 cordova-amazon-fireos/3.4.0 AmazonWebAppPlatform/3.4.0;2.0";

        for (int i = 0; i < 100000; i++) {
            Map<String, String> row = ParseUserAgent.parse(userAgent);
            assertEquals("Tablet",       row.get("DeviceClass"));
            assertEquals("FireOS 3.4.0", row.get("OperatingSystemNameVersion"));
        }
    }

    @Test
    void testErrorReport1557Header() {
        // https://github.com/nielsbasjes/yauaa/issues/1557
        // This is an edge case where the only a single value is given
        // which is the same as a recognized header name.
        String userAgent = "User-Agent";
        Map<String, String> row = ParseUserAgent.parse(userAgent);
        assertEquals("Hacker", row.get("DeviceClass"));
        assertEquals("Hacker", row.get("OperatingSystemNameVersion"));
    }

    @Test
    void testErrorReport1557Field() {
        // https://github.com/nielsbasjes/yauaa/issues/1557
        // This is an edge case where the only a single value is given
        // which is the same as a recognized field name.
        String userAgent = "DeviceClass";
        Map<String, String> row = ParseUserAgent.parse(userAgent);
        assertEquals("Hacker", row.get("DeviceClass"));
        assertEquals("Hacker", row.get("OperatingSystemNameVersion"));
    }

    @Test
    void testWithoutClientHints() {
        final int iterations = 100_000;
        int i;
        for (i = 0; i < iterations; i++) {
            Map<String, String> row = ParseUserAgent.parse(
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36"
            );

            assertEquals("Desktop",               row.get("DeviceClass"));
            assertEquals("Linux Desktop",         row.get("DeviceName"));
            assertEquals("Unknown",               row.get("DeviceBrand"));
            assertEquals("Intel x86_64",          row.get("DeviceCpu"));
            assertEquals("64",                    row.get("DeviceCpuBits"));
            assertEquals("Desktop",               row.get("OperatingSystemClass"));
            assertEquals("Linux",                 row.get("OperatingSystemName"));
            assertEquals("??",                    row.get("OperatingSystemVersion"));
            assertEquals("??",                    row.get("OperatingSystemVersionMajor"));
            assertEquals("Linux ??",              row.get("OperatingSystemNameVersion"));
            assertEquals("Linux ??",              row.get("OperatingSystemNameVersionMajor"));
            assertEquals("Browser",               row.get("LayoutEngineClass"));
            assertEquals("Blink",                 row.get("LayoutEngineName"));
            assertEquals("100.0",                 row.get("LayoutEngineVersion"));
            assertEquals("100",                   row.get("LayoutEngineVersionMajor"));
            assertEquals("Blink 100.0",           row.get("LayoutEngineNameVersion"));
            assertEquals("Blink 100",             row.get("LayoutEngineNameVersionMajor"));
            assertEquals("Browser",               row.get("AgentClass"));
            assertEquals("Chrome",                row.get("AgentName"));
            assertEquals("100.0.4896.127",        row.get("AgentVersion"));
            assertEquals("100",                   row.get("AgentVersionMajor"));
            assertEquals("Chrome 100.0.4896.127", row.get("AgentNameVersion"));
            assertEquals("Chrome 100",            row.get("AgentNameVersionMajor"));
        }
        assertEquals(iterations, i);
    }

    @Test
    void testClientHints() {
        final int iterations = 100_000;
        int i;
        for (i = 0; i < iterations; i++) {
            Map<String, String> row = ParseUserAgent.parse(
                "User-Agent",                   "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36",
                "Sec-Ch-Ua",                    "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"",
                "Sec-Ch-Ua-Arch",               "\"x86\"",
                "Sec-Ch-Ua-Bitness",            "\"64\"",
                "Sec-Ch-Ua-Full-Version",       "\"100.0.4896.127\"",
                "Sec-Ch-Ua-Full-Version-List",  "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.127\", \"Google Chrome\";v=\"100.0.4896.127\"",
                "Sec-Ch-Ua-Mobile",             "?0",
                "Sec-Ch-Ua-Model",              "\"\"",
                "Sec-Ch-Ua-Platform",           "\"Linux\"",
                "Sec-Ch-Ua-Platform-Version",   "\"5.13.0\"",
                "Sec-Ch-Ua-Wow64",              "?0"
            );

            assertEquals("Desktop",               row.get("DeviceClass"));
            assertEquals("Linux Desktop",         row.get("DeviceName"));
            assertEquals("Unknown",               row.get("DeviceBrand"));
            assertEquals("Intel x86_64",          row.get("DeviceCpu"));
            assertEquals("64",                    row.get("DeviceCpuBits"));
            assertEquals("Desktop",               row.get("OperatingSystemClass"));
            assertEquals("Linux",                 row.get("OperatingSystemName"));
            assertEquals("5.13.0",                row.get("OperatingSystemVersion"));
            assertEquals("5",                     row.get("OperatingSystemVersionMajor"));
            assertEquals("Linux 5.13.0",          row.get("OperatingSystemNameVersion"));
            assertEquals("Linux 5",               row.get("OperatingSystemNameVersionMajor"));
            assertEquals("Browser",               row.get("LayoutEngineClass"));
            assertEquals("Blink",                 row.get("LayoutEngineName"));
            assertEquals("100.0",                 row.get("LayoutEngineVersion"));
            assertEquals("100",                   row.get("LayoutEngineVersionMajor"));
            assertEquals("Blink 100.0",           row.get("LayoutEngineNameVersion"));
            assertEquals("Blink 100",             row.get("LayoutEngineNameVersionMajor"));
            assertEquals("Browser",               row.get("AgentClass"));
            assertEquals("Chrome",                row.get("AgentName"));
            assertEquals("100.0.4896.127",        row.get("AgentVersion"));
            assertEquals("100",                   row.get("AgentVersionMajor"));
            assertEquals("Chrome 100.0.4896.127", row.get("AgentNameVersion"));
            assertEquals("Chrome 100",            row.get("AgentNameVersionMajor"));
        }
        assertEquals(iterations, i);
    }

    @Test
    void testClientHintsAlternative() {
        final int iterations = 100_000;
        int i;
        for (i = 0; i < iterations; i++) {
            Map<String, String> row = ParseUserAgent.parse(
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36",
                "Sec-Ch-Ua",                    "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"",
                "Sec-Ch-Ua-Arch",               "\"x86\"",
                "Sec-Ch-Ua-Bitness",            "\"64\"",
                "Sec-Ch-Ua-Full-Version",       "\"100.0.4896.127\"",
                "Sec-Ch-Ua-Full-Version-List",  "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.127\", \"Google Chrome\";v=\"100.0.4896.127\"",
                "Sec-Ch-Ua-Mobile",             "?0",
                "Sec-Ch-Ua-Model",              "\"\"",
                "Sec-Ch-Ua-Platform",           "\"Linux\"",
                "Sec-Ch-Ua-Platform-Version",   "\"5.13.0\"",
                "Sec-Ch-Ua-Wow64",              "?0"
            );

            assertEquals("Desktop",               row.get("DeviceClass"));
            assertEquals("Linux Desktop",         row.get("DeviceName"));
            assertEquals("Unknown",               row.get("DeviceBrand"));
            assertEquals("Intel x86_64",          row.get("DeviceCpu"));
            assertEquals("64",                    row.get("DeviceCpuBits"));
            assertEquals("Desktop",               row.get("OperatingSystemClass"));
            assertEquals("Linux",                 row.get("OperatingSystemName"));
            assertEquals("5.13.0",                row.get("OperatingSystemVersion"));
            assertEquals("5",                     row.get("OperatingSystemVersionMajor"));
            assertEquals("Linux 5.13.0",          row.get("OperatingSystemNameVersion"));
            assertEquals("Linux 5",               row.get("OperatingSystemNameVersionMajor"));
            assertEquals("Browser",               row.get("LayoutEngineClass"));
            assertEquals("Blink",                 row.get("LayoutEngineName"));
            assertEquals("100.0",                 row.get("LayoutEngineVersion"));
            assertEquals("100",                   row.get("LayoutEngineVersionMajor"));
            assertEquals("Blink 100.0",           row.get("LayoutEngineNameVersion"));
            assertEquals("Blink 100",             row.get("LayoutEngineNameVersionMajor"));
            assertEquals("Browser",               row.get("AgentClass"));
            assertEquals("Chrome",                row.get("AgentName"));
            assertEquals("100.0.4896.127",        row.get("AgentVersion"));
            assertEquals("100",                   row.get("AgentVersionMajor"));
            assertEquals("Chrome 100.0.4896.127", row.get("AgentNameVersion"));
            assertEquals("Chrome 100",            row.get("AgentNameVersionMajor"));
        }
        assertEquals(iterations, i);
    }

    @Test
    void testClientHintsLimitedFieldSet() {
        final int iterations = 100_000;
        int i;
        for (i = 0; i < iterations; i++) {
            Map<String, String> row = ParseUserAgent.parse(
                "User-Agent",                   "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36",
                "Sec-Ch-Ua",                    "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"",
                "Sec-Ch-Ua-Arch",               "\"x86\"",
                "Sec-Ch-Ua-Bitness",            "\"64\"",
                "DeviceClass",
                "Sec-Ch-Ua-Full-Version",       "\"100.0.4896.127\"",
                "Sec-Ch-Ua-Full-Version-List",  "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.127\", \"Google Chrome\";v=\"100.0.4896.127\"",
                "Sec-Ch-Ua-Mobile",             "?0",
                "AgentNameVersion",
                "Sec-Ch-Ua-Model",              "\"\"",
                "Sec-Ch-Ua-Platform",           "\"Linux\"",
                "OperatingSystemNameVersion",
                "Sec-Ch-Ua-Platform-Version",   "\"5.13.0\"",
                "Sec-Ch-Ua-Wow64",              "?0"
            );

            assertEquals("Desktop",               row.get("DeviceClass"));
            assertEquals("Linux 5.13.0",          row.get("OperatingSystemNameVersion"));
            assertEquals("Chrome 100.0.4896.127", row.get("AgentNameVersion"));

            assertNull(row.get("DeviceName"));
            assertNull(row.get("DeviceBrand"));
            assertNull(row.get("DeviceCpu"));
            assertNull(row.get("DeviceCpuBits"));
            assertNull(row.get("OperatingSystemClass"));
            assertNull(row.get("OperatingSystemName"));
            assertNull(row.get("OperatingSystemVersion"));
            assertNull(row.get("OperatingSystemVersionMajor"));
            assertNull(row.get("OperatingSystemNameVersionMajor"));
            assertNull(row.get("LayoutEngineClass"));
            assertNull(row.get("LayoutEngineName"));
            assertNull(row.get("LayoutEngineVersion"));
            assertNull(row.get("LayoutEngineVersionMajor"));
            assertNull(row.get("LayoutEngineNameVersion"));
            assertNull(row.get("LayoutEngineNameVersionMajor"));
            assertNull(row.get("AgentClass"));
            assertNull(row.get("AgentName"));
            assertNull(row.get("AgentVersion"));
            assertNull(row.get("AgentVersionMajor"));
            assertNull(row.get("AgentNameVersionMajor"));
        }
        assertEquals(iterations, i);
    }

}

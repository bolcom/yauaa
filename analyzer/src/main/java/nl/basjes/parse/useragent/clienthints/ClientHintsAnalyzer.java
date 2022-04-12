/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

package nl.basjes.parse.useragent.clienthints;

import com.esotericsoftware.kryo.Kryo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.basjes.collections.PrefixMap;
import nl.basjes.collections.prefixmap.StringPrefixMap;
import nl.basjes.parse.useragent.AgentField.MutableAgentField;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.clienthints.ClientHints.BrandVersion;
import nl.basjes.parse.useragent.utils.VersionSplitter;
import nl.basjes.parse.useragent.utils.WordSplitter;

import java.io.Serializable;
import java.util.List;

import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_CLASS;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VALUE;
import static nl.basjes.parse.useragent.classify.DeviceClass.MOBILE;
import static nl.basjes.parse.useragent.classify.DeviceClass.PHONE;
import static nl.basjes.parse.useragent.classify.DeviceClass.TABLET;

public class ClientHintsAnalyzer extends ClientHintsHeadersParser {

    public ClientHintsAnalyzer() {
        super();
    }

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * The expected type here is Object because otherwise the Kryo library becomes
     * a mandatory dependency on any project that uses Yauaa.
     *
     * @param kryoInstance The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Object kryoInstance) {
        Kryo kryo = (Kryo) kryoInstance;
        kryo.register(ClientHintsAnalyzer.class);
        ClientHintsHeadersParser.configureKryo(kryoInstance);
    }

    // https://docs.microsoft.com/en-us/microsoft-edge/web-platform/how-to-detect-win11
    // Version      platformVersion
    // Win7/8/8.1   0
    // Win10        1-10
    // Win11        13+
    // https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform-version
    // If major is 6 and minor is 3 (i.e., Windows 8.1), return "0.3".
    // If major is 6 and minor is 2 (i.e., Windows 8), return "0.2".
    // If major is 6 and minor is 1 (i.e., Windows 7), return "0.1".

    @AllArgsConstructor
    private static class OSFields implements Serializable {
        @Getter
        String name;                      // Windows NT
        @Getter String version;           // 8.1
        @Getter String versionMajor;      // 8
        @Getter String nameVersion;       // Windows 8.1
        @Getter String nameVersionMajor;  // Windows 8
    }

    private static final PrefixMap<OSFields> WINDOWS_VERSION_MAPPING = new StringPrefixMap<>(false);
    static {
        WINDOWS_VERSION_MAPPING.put("0.1", new OSFields("Windows NT",  "7",   "7", "Windows 7",   "Windows 7"));
        WINDOWS_VERSION_MAPPING.put("0.2", new OSFields("Windows NT",  "8",   "8", "Windows 8",   "Windows 8"));
        WINDOWS_VERSION_MAPPING.put("0.3", new OSFields("Windows NT",  "8.1", "8", "Windows 8.1", "Windows 8"));
        WINDOWS_VERSION_MAPPING.put("1",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("2",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("3",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("4",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("5",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("6",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("7",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("8",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("9",   new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("10",  new OSFields("Windows NT", "10",  "10", "Windows 10",  "Windows 10"));
        WINDOWS_VERSION_MAPPING.put("13",  new OSFields("Windows NT", "11",  "11", "Windows 11",  "Windows 11"));
        WINDOWS_VERSION_MAPPING.put("14",  new OSFields("Windows NT", "11",  "11", "Windows 11",  "Windows 11"));
        WINDOWS_VERSION_MAPPING.put("15",  new OSFields("Windows NT", "11",  "11", "Windows 11",  "Windows 11"));
        WINDOWS_VERSION_MAPPING.put("16",  new OSFields("Windows NT", "11",  "11", "Windows 11",  "Windows 11"));
    }

    public MutableUserAgent merge(MutableUserAgent userAgent, ClientHints clientHints) {
        // FIXME: First version: Very hard coded analysis rules.
        detectVersionMismatchRobots(userAgent, clientHints);
        improveMobileDeviceClass(userAgent, clientHints);
        improveDeviceBrandName(userAgent, clientHints);
        improveOperatingSystem(userAgent, clientHints);
        improveLayoutEngineAndAgentInfo(userAgent, clientHints);
        return userAgent;
    }

    public void detectVersionMismatchRobots(MutableUserAgent userAgent, ClientHints clientHints) {
        List<BrandVersion> brandsList = clientHints.getBrands();
        if (brandsList != null && !brandsList.isEmpty()) {
            String majorVersion;

            MutableAgentField layoutEngineName = userAgent.get(LAYOUT_ENGINE_NAME);
            MutableAgentField layoutEngineVersionMajor = userAgent.get(LAYOUT_ENGINE_VERSION_MAJOR);
            MutableAgentField agentName = userAgent.get(AGENT_NAME);
            MutableAgentField agentVersionMajor = userAgent.get(AGENT_VERSION_MAJOR);
            MutableAgentField deviceClass = userAgent.get(DEVICE_CLASS);
            for (BrandVersion brandVersion : brandsList) {
                String[] versionSplits = userAgent.getValue(AGENT_VERSION).split("\\.");
                if (brandVersion.getVersion().equals("99")) {
                    if (versionSplits.length == 2 && "0".equals(versionSplits[1])) {
                        continue;
                    }
                }
                majorVersion = brandVersion.getVersion().split("\\.")[0];

                switch (brandVersion.getBrand()) {
                    case "Chromium":
                        if (layoutEngineName.getValue().equals("Blink")) {
                            if (!layoutEngineVersionMajor.getValue().equals(majorVersion)) {
                                deviceClass.setValue("Robot", deviceClass.getConfidence() + 1);
                            }
                        }
                        break;
                    case "Google Chrome":
                    case "Chrome":
                        if (agentName.getValue().equals("Chrome")) {
                            if (!agentVersionMajor.getValue().equals(majorVersion)) {
                                deviceClass.setValue("Robot", deviceClass.getConfidence() + 1);
                            }
                        }
                        break;
                    case "Microsoft Edge":
                    case "Edge":
                        if (agentName.getValue().equals("Edge")) {
                            if (!agentVersionMajor.getValue().equals(majorVersion)) {
                                deviceClass.setValue("Robot", deviceClass.getConfidence() + 1);
                            }
                        }
                        break;
                    case "Opera":
                        if (agentName.getValue().equals("Opera") && !userAgent.getValue(OPERATING_SYSTEM_CLASS).equals(MOBILE.getValue())) {
                            if (!agentVersionMajor.getValue().equals(majorVersion)) {
                                deviceClass.setValue("Robot", deviceClass.getConfidence() + 1);
                            }
                        }
                        break;
                    case "OperaMobile":
                        if (agentName.getValue().equals("Opera") && userAgent.getValue(OPERATING_SYSTEM_CLASS).equals(MOBILE.getValue())) {
                            if (!agentVersionMajor.getValue().equals(majorVersion)) {
                                deviceClass.setValue("Robot", deviceClass.getConfidence() + 1);
                            }
                        }
                        break;
                    default: // Ignore all others
                }
            }
        }

    }

    public void improveMobileDeviceClass(MutableUserAgent userAgent, ClientHints clientHints) {
        // Improve the Device Class if it is the vague "Mobile" thing.
        if (clientHints.getMobile() != null) {
            MutableAgentField deviceClass = userAgent.get(UserAgent.DEVICE_CLASS);
            if (MOBILE.getValue().equals(deviceClass.getValue())) {
                if (Boolean.TRUE.equals(clientHints.getMobile())) {
                    deviceClass.setValue(PHONE.getValue(), deviceClass.getConfidence() + 1);
                } else {
                    deviceClass.setValue(TABLET.getValue(), deviceClass.getConfidence() + 1);
                }
            }
        }
    }

    public void improveDeviceBrandName(MutableUserAgent userAgent, ClientHints clientHints) {
        // Improve the Device Brand/Name if it is unknown.
        if (clientHints.getModel() != null) {
            MutableAgentField deviceBrand = userAgent.get(DEVICE_BRAND);
            MutableAgentField deviceName = userAgent.get(DEVICE_NAME);
            if (UNKNOWN_VALUE.equals(deviceBrand.getValue()) ||
                UNKNOWN_VALUE.equals(deviceName.getValue())) {
                overrideValue(deviceBrand, WordSplitter.getInstance().getSingleSplit(clientHints.getModel(), 1));
                overrideValue(deviceName, clientHints.getModel());
            }
        }
    }

    public void improveOperatingSystem(MutableUserAgent userAgent, ClientHints clientHints) {
        // Improve the OS info.
        // https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform
        // The Sec-CH-UA-Platform request header field gives a server information about the platform on which
        // a given user agent is executing. It is a Structured Header whose value MUST be a string [RFC8941].
        // Its value SHOULD match one of the following common platform values:
        // - "Android"
        // - "Chrome OS"
        // - "iOS"
        // - "Linux"
        // - "macOS"
        // - "Windows"
        // - "Unknown"
        String platform = clientHints.getPlatform();
        String platformVersion = clientHints.getPlatformVersion();
        if (platform != null && platformVersion != null && !platform.trim().isEmpty() && !platformVersion.trim().isEmpty()) {
//            MutableAgentField osName    = (MutableAgentField) userAgent.get(UserAgent.OPERATING_SYSTEM_NAME);
            String majorVersion = VersionSplitter.getInstance().getSingleSplit(platformVersion, 1);
            switch (platform) {
                case "macOS":
                    platform = "Mac OS";
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME),               platform);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION),            platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION_MAJOR),      majorVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION),       platform + " " + platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION_MAJOR), platform + " " + majorVersion);
                    break;

                case "Android":
                case "Chrome OS":
                case "iOS":
                case "Linux":
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME),               platform);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION),            platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION_MAJOR),      majorVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION),       platform + " " + platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION_MAJOR), platform + " " + majorVersion);
                    break;

                case "Windows":
                    OSFields betterOsVersion = WINDOWS_VERSION_MAPPING.getLongestMatch(platformVersion);
                    if (betterOsVersion != null) {
                        overrideValue(userAgent.get(OPERATING_SYSTEM_NAME),               betterOsVersion.getName());
                        overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION),            betterOsVersion.getVersion());
                        overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION_MAJOR),      betterOsVersion.getVersionMajor());
                        overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION),       betterOsVersion.getNameVersion());
                        overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION_MAJOR), betterOsVersion.getNameVersionMajor());
                        // FIXME: Add         OperatingSystemVersionBuild          : '??'
                    }
                    break;

                case "Unknown":
                default:
                    platform = userAgent.getValue(OPERATING_SYSTEM_NAME);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION), platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_VERSION_MAJOR), majorVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION), platform + " " + platformVersion);
                    overrideValue(userAgent.get(OPERATING_SYSTEM_NAME_VERSION_MAJOR), platform + " " + majorVersion);
                    break;
            }
        }
    }

    public void improveLayoutEngineAndAgentInfo(MutableUserAgent userAgent, ClientHints clientHints) {
        // Improve the Agent info.
        List<BrandVersion> fullVersionList = clientHints.getFullVersionList();
        if (fullVersionList != null && !fullVersionList.isEmpty()) {
            String version;
            String majorVersion;

            String agentName;
            for (BrandVersion brandVersion : fullVersionList) {
                String[] versionSplits;
                switch (brandVersion.getBrand()) {
                    case "Chromium":
                        version = brandVersion.getVersion();
                        versionSplits = version.split("\\.");
                        if (versionSplits.length == 4) {
                            if (!"0".equals(versionSplits[1])) {
                                continue;
                            }
                        }
                        version = versionSplits[0] + '.' + versionSplits[1];
                        majorVersion = versionSplits[0];
                        overrideValue(userAgent.get(LAYOUT_ENGINE_NAME), "Blink");
                        overrideValue(userAgent.get(LAYOUT_ENGINE_VERSION), version);
                        overrideValue(userAgent.get(LAYOUT_ENGINE_NAME_VERSION), "Blink " + version);
                        overrideValue(userAgent.get(LAYOUT_ENGINE_VERSION_MAJOR), majorVersion);
                        overrideValue(userAgent.get(LAYOUT_ENGINE_NAME_VERSION_MAJOR), "Blink "+ majorVersion);
                        break;

                    case "Google Chrome":
                    case "Chrome":
                        agentName = "Chrome";
                        version = brandVersion.getVersion();
                        versionSplits = version.split("\\.");
                        if (versionSplits.length == 4) {
                            if (!"0".equals(versionSplits[1])) {
                                continue;
                            }
                        }
                        majorVersion = versionSplits[0];

                        overrideValue(userAgent.get(AGENT_NAME), agentName);
                        overrideValue(userAgent.get(AGENT_VERSION), version);
                        overrideValue(userAgent.get(AGENT_NAME_VERSION), agentName + " " + version);
                        overrideValue(userAgent.get(AGENT_VERSION_MAJOR), majorVersion);
                        overrideValue(userAgent.get(AGENT_NAME_VERSION_MAJOR), agentName + " " + majorVersion);
                        break;

                    case "Microsoft Edge":
                    case "Edge":
                        agentName = "Edge";
                        version = brandVersion.getVersion();
                        versionSplits = version.split("\\.");
                        if (versionSplits.length == 4) {
                            if (!"0".equals(versionSplits[1])) {
                                continue;
                            }
                        }
                        majorVersion = versionSplits[0];

                        overrideValue(userAgent.get(AGENT_NAME), agentName);
                        overrideValue(userAgent.get(AGENT_VERSION), version);
                        overrideValue(userAgent.get(AGENT_NAME_VERSION), agentName + " " + version);
                        overrideValue(userAgent.get(AGENT_VERSION_MAJOR), majorVersion);
                        overrideValue(userAgent.get(AGENT_NAME_VERSION_MAJOR), agentName + " " + majorVersion);
                        break;
                    default:
                        // Ignore
                }
            }
        }
    }

    private void overrideValue(MutableAgentField field, String newValue) {
        field.setValue(newValue, field.getConfidence()+1);
    }

}

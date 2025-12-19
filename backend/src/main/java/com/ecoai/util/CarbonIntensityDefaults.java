package com.ecoai.util;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default carbon intensity factors by country/region.
 * Values are in gCO₂/kWh (grams of CO₂ per kilowatt-hour).
 * 
 * Source: IEA (International Energy Agency) and national grid data.
 * These are approximations - actual values vary by time and source.
 */
public class CarbonIntensityDefaults {

    private static final Map<String, RegionData> DEFAULTS = new LinkedHashMap<>();

    static {
        // High carbon intensity regions
        DEFAULTS.put("IN", new RegionData("India", new BigDecimal("708")));
        DEFAULTS.put("AU", new RegionData("Australia", new BigDecimal("656")));
        DEFAULTS.put("CN", new RegionData("China", new BigDecimal("555")));
        DEFAULTS.put("PL", new RegionData("Poland", new BigDecimal("650")));
        DEFAULTS.put("ZA", new RegionData("South Africa", new BigDecimal("900")));

        // Medium carbon intensity regions
        DEFAULTS.put("US", new RegionData("United States", new BigDecimal("386")));
        DEFAULTS.put("JP", new RegionData("Japan", new BigDecimal("457")));
        DEFAULTS.put("DE", new RegionData("Germany", new BigDecimal("350")));
        DEFAULTS.put("UK", new RegionData("United Kingdom", new BigDecimal("233")));
        DEFAULTS.put("IT", new RegionData("Italy", new BigDecimal("315")));

        // Low carbon intensity regions
        DEFAULTS.put("EU", new RegionData("European Union (Avg)", new BigDecimal("276")));
        DEFAULTS.put("CA", new RegionData("Canada", new BigDecimal("120")));
        DEFAULTS.put("FR", new RegionData("France", new BigDecimal("56")));
        DEFAULTS.put("SE", new RegionData("Sweden", new BigDecimal("41")));
        DEFAULTS.put("NO", new RegionData("Norway", new BigDecimal("26")));

        // Cloud provider regions (approximate based on location)
        DEFAULTS.put("US-EAST", new RegionData("AWS US East", new BigDecimal("380")));
        DEFAULTS.put("US-WEST", new RegionData("AWS US West", new BigDecimal("300")));
        DEFAULTS.put("EU-WEST", new RegionData("AWS EU West (Ireland)", new BigDecimal("296")));
        DEFAULTS.put("EU-NORTH", new RegionData("AWS EU North (Stockholm)", new BigDecimal("45")));
        DEFAULTS.put("AP-SOUTH", new RegionData("AWS Asia Pacific (Mumbai)", new BigDecimal("708")));
    }

    public static BigDecimal getIntensity(String regionCode) {
        RegionData data = DEFAULTS.get(regionCode.toUpperCase());
        return data != null ? data.intensity : new BigDecimal("400"); // Default global average
    }

    public static String getRegionName(String regionCode) {
        RegionData data = DEFAULTS.get(regionCode.toUpperCase());
        return data != null ? data.name : regionCode;
    }

    public static Map<String, RegionData> getAllDefaults() {
        return new LinkedHashMap<>(DEFAULTS);
    }

    public static class RegionData {
        public final String name;
        public final BigDecimal intensity;

        public RegionData(String name, BigDecimal intensity) {
            this.name = name;
            this.intensity = intensity;
        }
    }
}

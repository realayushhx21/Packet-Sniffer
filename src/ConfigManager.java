package com.first;

import java.io.*;
import java.util.Properties;

/**
 * Centralized configuration manager that loads settings from config.properties.
 * Used by all features: API keys, DB paths, thresholds, etc.
 */
public class ConfigManager {

    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties = null;

    /**
     * Load properties from config.properties file.
     * Searches in current directory, then classpath.
     */
    public static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            // Try loading from file system first
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                } catch (IOException e) {
                    System.err.println("Warning: Could not load " + CONFIG_FILE + ": " + e.getMessage());
                }
            } else {
                // Try classpath
                try (InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                    if (is != null) {
                        properties.load(is);
                    } else {
                        System.err.println("Warning: " + CONFIG_FILE + " not found. Using defaults.");
                    }
                } catch (IOException e) {
                    System.err.println("Warning: Could not load " + CONFIG_FILE + ": " + e.getMessage());
                }
            }
        }
        return properties;
    }

    public static String getGeminiApiKey() {
        return getProperties().getProperty("gemini.api.key", "");
    }

    public static String getDbPath() {
        return getProperties().getProperty("db.path", "packet_sniffer.db");
    }

    public static int getCaptureSnaplen() {
        return Integer.parseInt(getProperties().getProperty("capture.snaplen", "65536"));
    }

    public static int getCaptureTimeout() {
        return Integer.parseInt(getProperties().getProperty("capture.timeout", "150"));
    }

    public static int getChartHistorySeconds() {
        return Integer.parseInt(getProperties().getProperty("chart.history.seconds", "30"));
    }

    public static int getChartUpdateIntervalMs() {
        return Integer.parseInt(getProperties().getProperty("chart.update.interval.ms", "1000"));
    }

    public static int getPortScanThreshold() {
        return Integer.parseInt(getProperties().getProperty("threat.portscan.threshold", "10"));
    }

    public static int getPortScanWindowSeconds() {
        return Integer.parseInt(getProperties().getProperty("threat.portscan.window.seconds", "5"));
    }

    public static int getDosThreshold() {
        return Integer.parseInt(getProperties().getProperty("threat.dos.threshold", "200"));
    }

    public static int getDosWindowSeconds() {
        return Integer.parseInt(getProperties().getProperty("threat.dos.window.seconds", "3"));
    }

    /**
     * Save a property back to the config file.
     */
    public static void setProperty(String key, String value) {
        getProperties().setProperty(key, value);
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Packet Sniffer Configuration");
        } catch (IOException e) {
            System.err.println("Warning: Could not save " + CONFIG_FILE + ": " + e.getMessage());
        }
    }
}

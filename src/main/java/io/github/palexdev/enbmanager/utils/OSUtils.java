package io.github.palexdev.enbmanager.utils;

import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

@Component
public class OSUtils {
    //================================================================================
    // Internal Classes
    //================================================================================
    enum OSType {
        Windows, Linux, MacOS, Other
    }

    //================================================================================
    // Properties
    //================================================================================
    private SystemInfo si;
    private OperatingSystem os;
    private static String osInfo;
    private static OSType osType;

    //================================================================================
    // Constructors
    //================================================================================
    public OSUtils() {}

    //================================================================================
    // Methods
    //================================================================================
    public SystemInfo getSystemInfo() {
        if (si == null) si = new SystemInfo();
        return si;
    }

    public OperatingSystem getOS() {
        if (os == null) os = getSystemInfo().getOperatingSystem();
        return os;
    }

    public String getOSInfo() {
        if (osInfo == null) osInfo = getOS().toString().toLowerCase();
        return osInfo;
    }

    public OSType getOSType() {
        if (osType == null) osType = detectOS();
        return osType;
    }

    protected OSType detectOS() {
        return switch (getOSInfo()) {
            case String i when i.contains("win") -> OSType.Windows;
            case String i when (i.contains("nix") || i.contains("nux")) -> OSType.Linux;
            case String i when i.contains("mac") -> OSType.MacOS;
            default -> OSType.Other;
        };
    }
}

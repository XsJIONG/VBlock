package com.mojang.minecraftpe;

import android.os.Build;
import android.os.Build.VERSION;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class HardwareInformation {
    private static String cpuFeatures = "unknown";
    private static String cpuName = "unknown";
    private static int numCores = 1;

    static {
        initHardwareInformation();
    }

    public static String getDeviceModelName() {
        return Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL;
    }

    public static String getAndroidVersion() {
        return "Android " + VERSION.RELEASE;
    }

    public static String getCPUType() {
        return Build.CPU_ABI;
    }

    public static String getCPUName() {
        return cpuName;
    }

    public static String getCPUFeatures() {
        return cpuFeatures;
    }

    public static int getNumCores() {
        return numCores;
    }

    public static void initHardwareInformation() {
        try {
            numCores = getNumCoresReal();
            parseCpuInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getNumCoresReal() {
        try {
            return new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return Pattern.matches("cpu[0-9]+", pathname.getName());
                }
            }).length;
        } catch (Exception e) {
            return Math.max(1, Runtime.getRuntime().availableProcessors());
        }
    }

    private static void parseCpuInfo() throws IOException {
        Throwable th;
        BufferedReader reader = null;
        boolean cpuNameDone = false;
        boolean cpuFeaturesDone = false;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader("/proc/cpuinfo"));
            while (true) {
                try {
                    String l = reader2.readLine();
                    if (l == null) {
                        break;
                    } else if (l.contains(":")) {
                        String[] parts = l.split(":");
                        String partName = parts[0].trim();
                        String result = parts[1].trim();
                        if (partName.equals("Hardware") || partName.equals("model name")) {
                            cpuName = result;
                            cpuNameDone = true;
                        } else if (partName.equals("Features") || partName.equals("flags")) {
                            cpuFeatures = result;
                            cpuFeaturesDone = true;
                        }
                        if (cpuNameDone && cpuFeaturesDone) {
                            break;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                }
            }
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e) {
                }
            }
        } catch (Throwable th3) {
            th = th3;
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {
                }
            }
            throw new RuntimeException(th);
        }
    }
}

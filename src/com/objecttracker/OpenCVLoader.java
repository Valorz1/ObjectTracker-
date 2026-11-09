package com.objecttracker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;

public final class OpenCVLoader {
    private static final String[] LIB_PATH_ENV_VARS = {
            "OPENCV_HOME",
            "OPENCV_DIR",
            "OPENCV_LIB_PATH"
    };

    private OpenCVLoader() {
    }

    public static void load() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            return;
        } catch (UnsatisfiedLinkError ignored) {
            // Fall through to a more explicit resolution strategy.
        }

        String explicitLibraryPath = System.getenv("OPENCV_LIB_PATH");
        if (explicitLibraryPath != null && !explicitLibraryPath.trim().isEmpty()) {
            File explicitFile = new File(explicitLibraryPath);
            if (explicitFile.exists()) {
                System.load(explicitFile.getAbsolutePath());
                return;
            }
        }

        for (String envName : LIB_PATH_ENV_VARS) {
            String basePath = System.getenv(envName);
            if (basePath == null || basePath.trim().isEmpty()) {
                continue;
            }

            List<String> candidates = buildCandidates(basePath);
            if (tryLoadCandidates(candidates)) {
                return;
            }
        }

        String[] defaultLocations = {
                "/usr/local/lib",
                "/usr/lib",
                "/usr/local/share/java",
                "/usr/share/java",
                "/opt/opencv/lib",
                "/opt/opencv/build/lib",
                "/opt/opencv/build/java"
        };

        if (tryLoadCandidates(defaultLocations)) {
            return;
        }

        throw new IllegalStateException(
                "OpenCV native library could not be found. Set OPENCV_HOME or OPENCV_LIB_PATH " +
                "to the OpenCV installation directory or library path and try again.");
    }

    private static boolean tryLoadCandidates(String... candidates) {
        return tryLoadCandidates(List.of(candidates));
    }

    private static boolean tryLoadCandidates(List<String> candidates) {
        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File child : files) {
                        if (child.getName().startsWith("libopencv_java") || child.getName().startsWith("opencv_java")) {
                            try {
                                System.load(child.getAbsolutePath());
                                return true;
                            } catch (UnsatisfiedLinkError ignored) {
                                // Try the next candidate.
                            }
                        }
                    }
                }
            } else if (file.exists()) {
                try {
                    System.load(file.getAbsolutePath());
                    return true;
                } catch (UnsatisfiedLinkError ignored) {
                    // Try the next candidate.
                }
            }
        }
        return false;
    }

    private static List<String> buildCandidates(String basePath) {
        List<String> candidates = new ArrayList<>();
        String normalizedBase = basePath.trim();
        String[] likelyPaths = {
                normalizedBase,
                normalizedBase + "/build/lib",
                normalizedBase + "/lib",
                normalizedBase + "/lib64",
                normalizedBase + "/build/java",
                normalizedBase + "/build/java/x64",
                normalizedBase + "/java",
                normalizedBase + "/bin",
                normalizedBase + "/share/java"
        };

        for (String candidate : likelyPaths) {
            if (!candidate.isEmpty()) {
                candidates.add(candidate);
            }
        }
        return candidates;
    }
}

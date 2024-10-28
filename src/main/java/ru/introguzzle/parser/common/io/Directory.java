package ru.introguzzle.parser.common.io;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.net.URISyntaxException;

@UtilityClass
final class Directory {
    static String getRoot() {
        return isRunningFromJAR() ? getCurrentJARDirectory() : getCurrentProjectDirectory();
    }

    static String getJARName() {
        return new File(Directory.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
    }

    static boolean isRunningFromJAR() {
        return getJARName().contains(".jar");
    }

    static String getCurrentProjectDirectory() {
        return new File("").getAbsolutePath();
    }

    static String getCurrentJARDirectory() {
        try {
            return new File(Directory.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath()
            ).getParent();

        } catch (URISyntaxException e) {
            return null;
        }
    }
}
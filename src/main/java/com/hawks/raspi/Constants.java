package com.hawks.raspi;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Constants {
    public static String password = null;
    public static String base_url = "https://raspi-2slp.onrender.com/";

    public static String BASE_DIR = "uploads";
    public static String IMAGE_DIR = BASE_DIR + "/images/";
    public static String VIDEO_DIR = BASE_DIR + "/videos/";
    public static String AUDIO_DIR = BASE_DIR + "/audios/";


    // E:\Springboot\InstaAutomation --> windows
    // /home/ram/Downloads--> rasp5
    public static String SYSTEM_DIR = System.getProperty("user.dir") + "/";

    public static boolean amIRaspi;

    {
        try {
            String model = new String(Files.readAllBytes(Paths.get("/proc/device-tree/model")));
            if (model.toLowerCase().contains("raspberry")) {
                amIRaspi = true;
            } else {
                amIRaspi = false;
            }
        } catch (Exception e) {
            amIRaspi = false;
        }
    }

    public static boolean amIWin;

    {
        String os = System.getProperty("os.name").toLowerCase();
        amIWin = os.contains("win");
    }


}

package com.mcjty.rftools;

import cpw.mods.fml.common.FMLLog;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;

/**
 * In game settable preferences.
 */
public class Preferences {

    public static final String CATEGORY_USER = "user";
    private static boolean loaded = false;

    private static int buffBarX = 2;
    private static int buffBarY = 2;

    private static void init() {
        if (loaded) {
            return;
        }
        loaded = true;
        File file = new File(CommonProxy.modConfigDir.getPath() + File.separator + "rftools", "usersettings.cfg");
        Configuration cfg = new Configuration(file);
        try {
            setupConfig(cfg);
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "Problem loading usersettings.cfg file!");
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    private static void setupConfig(Configuration cfg) {
        cfg.load();
        cfg.addCustomCategoryComment(CATEGORY_USER, "User settings (editable in-game)");
        buffBarX = cfg.get(CATEGORY_USER, "buffBarX", buffBarX,
                "Top level coordinate of the buff bar. Use -1 to disable this bar").getInt();
        buffBarY = cfg.get(CATEGORY_USER, "buffBarY", buffBarY,
                "Top level coordinate of the buff bar. Use -1 to disable this bar").getInt();
    }

    private static Configuration startChange() {
        File file = new File(CommonProxy.modConfigDir.getPath() + File.separator + "rftools", "usersettings.cfg");
        Configuration cfg = new Configuration(file);
        try {
            setupConfig(cfg);
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "Problem loading usersettings.cfg file!");
        }
        return cfg;
    }

    private static void changeIntProperty(Configuration cfg, int value, String propertyName) {
        try {
            cfg.getCategory(CATEGORY_USER).get(propertyName).set(value);
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "Problem loading usersettings.cfg file!");
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    public static int getBuffBarX() {
        init();
        return buffBarX;
    }

    public static void setBuffBarX(int buffBarX) {
        init();
        Configuration cfg = startChange();
        Preferences.buffBarX = buffBarX;
        changeIntProperty(cfg, buffBarX, "buffBarX");

    }

    public static int getBuffBarY() {
        init();
        return buffBarY;
    }

    public static void setBuffBarY(int buffBarY) {
        init();
        Configuration cfg = startChange();
        Preferences.buffBarY = buffBarY;
        changeIntProperty(cfg, buffBarY, "buffBarY");
    }
}

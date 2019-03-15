package mcjty.rftools.blocks.teleporter;

import mcjty.lib.thirteen.ConfigSpec;
import mcjty.lib.varia.Logging;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class TeleportConfiguration {
    public static final String CATEGORY_TELEPORTER = "teleporter";

    public static ConfigSpec.IntValue TRANSMITTER_MAXENERGY;
    public static ConfigSpec.IntValue TRANSMITTER_RECEIVEPERTICK;
    public static ConfigSpec.IntValue RECEIVER_MAXENERGY;
    public static ConfigSpec.IntValue RECEIVER_RECEIVEPERTICK;
    public static ConfigSpec.IntValue DIALER_MAXENERGY;
    public static ConfigSpec.IntValue DIALER_RECEIVEPERTICK;
    public static ConfigSpec.IntValue horizontalDialerRange;           // Horizontal range the dialing device can check for transmitters
    public static ConfigSpec.IntValue verticalDialerRange;              // Vertical range the dialing device can check for transmitters
    public static ConfigSpec.IntValue rfPerDial;                     // RF Consumed by dialing device when making a new dial
    public static ConfigSpec.IntValue rfPerCheck;                    // RF Used to do a check on a receiver.
    public static ConfigSpec.IntValue rfDialedConnectionPerTick;       // RF Consumed by transmitter when a dial is active and not doing anything else

    public static ConfigSpec.IntValue ADVANCED_CHARGEDPORTER_MAXENERGY;     // Maximum RF capacity of a charged porter item. Teleporting costs 50% more then normal so keep this into account
    public static ConfigSpec.IntValue CHARGEDPORTER_MAXENERGY;     // Maximum RF capacity of a charged porter item. Teleporting costs 50% more then normal so keep this into account
    public static ConfigSpec.IntValue CHARGEDPORTER_RECEIVEPERTICK;

    public static ConfigSpec.IntValue advancedSpeedBonus;               // How much faster the speed of the advanced porter is

    // The following flags are used to calculate power usage for even starting a teleport. The rfStartTeleportBaseDim (cost of
    // teleporting to another dimension) is also the cap of the local teleport which is calculated by doing
    // rfStartTelelportBaseLocal + dist * rfStartTeleportDist
    public static ConfigSpec.IntValue rfStartTeleportBaseLocal;      // Base RF consumed by transmitter when starting a teleport in same dimension
    public static ConfigSpec.IntValue rfStartTeleportBaseDim;      // Base RF consumed by transmitter when starting a teleport to another dimension
    public static ConfigSpec.IntValue rfStartTeleportDist;             // RF per distance unit when starting a teleport
    public static ConfigSpec.IntValue rfTeleportPerTick;              // During the time the teleport is busy this RF is used per tick on the transmitter
    public static ConfigSpec.IntValue rfMatterIdleTick;                 // The rf per tick a dialed transmitter consumes.
    public static ConfigSpec.IntValue rfPerTeleportReceiver;         // On the receiver side we need this amount of power
    public static ConfigSpec.IntValue rfBoostedTeleport;            // The RF needed to do a boosted teleportation

    // The following flags are used to calculate the time used for doing the actual teleportation. Same principle as with
    // the power usage above with regards to local/dimensional teleport.
    public static ConfigSpec.IntValue timeTeleportBaseLocal;
    public static ConfigSpec.IntValue timeTeleportBaseDim;
    public static ConfigSpec.IntValue timeTeleportDist;                // Value in militicks (1000 == 1 tick)

    // Base volume for the teleporting sound (whoosh!)
    public static ConfigSpec.DoubleValue teleportVolume;
    public static ConfigSpec.DoubleValue teleportErrorVolume;

    // Set these flags if you want the matter transmitter to more aggressively check for receiver quality. Possibly with some performance penalty.
    public static ConfigSpec.IntValue matterTransmitterLoadChunk;
    public static ConfigSpec.IntValue matterTransmitterLoadWorld;

    public static ConfigSpec.BooleanValue whooshMessage;

    // Prevent inter-dimensional teleportation.
    public static ConfigSpec.BooleanValue preventInterdimensionalTeleports;
    // Blacklist the following dimensions to be able to teleport from.
    public static ConfigSpec.ConfigValue<String> blacklistedTeleportationSources;
    private static Set<Integer> blacklistedTeleportationSourcesSet = null;
    // Blacklist the following dimensions to be able to teleport too.
    public static ConfigSpec.ConfigValue<String> blacklistedTeleportationDestinations;
    private static Set<Integer> blacklistedTeleportationDestinationsSet = null;

    public static ConfigSpec.BooleanValue logTeleportUsages;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the teleportation system").push(CATEGORY_TELEPORTER);
        CLIENT_BUILDER.comment("Settings for the teleportation system").push(CATEGORY_TELEPORTER);

        TRANSMITTER_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the matter transmitter can hold. This should be at least equal to 'rfStartTeleportDim'")
                .defineInRange("transmitterMaxRF", 200000, 0, Integer.MAX_VALUE);
        TRANSMITTER_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the matter transmitter can receive. It is recommended to keep this at least equal to 'rfTeleportPerTick'")
                .defineInRange("transmitterRFPerTick", 1000, 0, Integer.MAX_VALUE);

        RECEIVER_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the matter receiver can hold")
                .defineInRange("receiverMaxRF", 100000, 0, Integer.MAX_VALUE);
        RECEIVER_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the matter receiver can receive")
                .defineInRange("receiverRFPerTick", 500, 0, Integer.MAX_VALUE);

        DIALER_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the dialing device can hold")
                .defineInRange("dialerMaxRF", 50000, 0, Integer.MAX_VALUE);
        DIALER_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the dialing device can receive")
                .defineInRange("dialerRFPerTick", 100, 0, Integer.MAX_VALUE);

        ADVANCED_CHARGEDPORTER_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the advanced charged porter item can hold (note that teleporting this way uses 50% more RF then with a matter transmitter)")
                .defineInRange("advancedChargedPorterMaxRF", 1000000, 0, Integer.MAX_VALUE);
        CHARGEDPORTER_MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the charged porter item can hold (note that teleporting this way uses 50% more RF then with a matter transmitter)")
                .defineInRange("chargedPorterMaxRF", 200000, 0, Integer.MAX_VALUE);
        CHARGEDPORTER_RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the the charged porter item can receive")
                .defineInRange("chargedPorterRFPerTick", 2000, 0, Integer.MAX_VALUE);
        advancedSpeedBonus = SERVER_BUILDER
                .comment("The speed bonus for the advanced charged porter (compared to the normal one)")
                .defineInRange("advancedSpeedBonus", 4, 0, Integer.MAX_VALUE);

        horizontalDialerRange = SERVER_BUILDER
                .comment("The horizontal range the dialing device uses to check for transmitters. These are the transmitters the dialing device will be able to control")
                .defineInRange("horizontalDialerRange", 10, 0, Integer.MAX_VALUE);
        verticalDialerRange = SERVER_BUILDER
                .comment("The vertical range the dialing device uses to check for transmitters")
                .defineInRange("verticalDialerRange", 5, 0, Integer.MAX_VALUE);

        rfPerDial = SERVER_BUILDER
                .comment("The amount of RF consumed when dialing a transmitter to another receiver")
                .defineInRange("rfPerDial", 1000, 0, Integer.MAX_VALUE);
        rfPerCheck = SERVER_BUILDER
                .comment("The amount of RF consumed when the dialing device checks for the capabilities of a receiver ('Check' button)")
                .defineInRange("rfPerCheck", 5000, 0, Integer.MAX_VALUE);
        rfDialedConnectionPerTick = SERVER_BUILDER
                .comment("The amount of RF that is consumed by the matter transmitter when a dial is active")
                .defineInRange("rfDialedConnectionPerTick", 10, 0, Integer.MAX_VALUE);

        rfStartTeleportBaseLocal = SERVER_BUILDER
                .comment("The amount of RF that is consumed by a matter transmitter when the player goes to stand in the teleportation beam allowing the teleportation process to start. This value is used for a teleport in the same dimension. In addition to this value the 'rfStartTeleportDist' is also added per traveled distance")
                .defineInRange("rfStartTeleportLocal", 5000, 0, Integer.MAX_VALUE);
        rfStartTeleportBaseDim = SERVER_BUILDER
                .comment("The amount of RF that is consumed by a matter transmitter when the player goes to stand in the teleportation beam allowing the teleportation process to start. This version is for a teleportation to another dimension and in this case 'rfStartTeleportDist' is not used. This value also acts as the maximum rf that can be consumed for a local teleport")
                .defineInRange("rfStartTeleportDim", 100000, 0, Integer.MAX_VALUE);
        rfStartTeleportDist = SERVER_BUILDER
                .comment("For every unit in distance this value is added to the initial RF cost for starting the teleportation. This value is not used when teleporting to another dimension")
                .defineInRange("rfStartTeleportDist", 10, 0, Integer.MAX_VALUE);
        rfTeleportPerTick = SERVER_BUILDER
                .comment("For the duration of the teleport process this value represents the amount of RF that is consumed by the matter transmitter for every tick")
                .defineInRange("rfTeleportPerTick", 500, 0, Integer.MAX_VALUE);
        rfMatterIdleTick = SERVER_BUILDER
                .comment("The amount of RF/tick an idle dialed transmitter consumes")
                .defineInRange("rfMatterIdleTick", 0, 0, Integer.MAX_VALUE);

        rfPerTeleportReceiver = SERVER_BUILDER
                .comment("This is the amount of RF that is consumed at the receiving side for every teleport. This RF is only consumed when the teleportation actually happens")
                .defineInRange("rfPerTeleportReceiver", 5000, 0, Integer.MAX_VALUE);
        rfBoostedTeleport = SERVER_BUILDER
                .comment("This is the amount of RF that is consumed at a boosted transmitter in case the receiver doesn't have enough power")
                .defineInRange("rfBoostedTeleport", 20000, 0, Integer.MAX_VALUE);

        timeTeleportBaseLocal = SERVER_BUILDER
                .comment("The base time used for a teleportation for a local teleport. The 'timeTeleportDist' value is added per distance traveled")
                .defineInRange("timeTeleportBaseLocal", 5, 0, Integer.MAX_VALUE);
        timeTeleportBaseDim = SERVER_BUILDER
                .comment("The base time used for a teleportation to another dimension. The 'timeTeleportDist' value is not used")
                .defineInRange("timeTeleportBaseDim", 50, 0, Integer.MAX_VALUE);
        timeTeleportDist = SERVER_BUILDER
                .comment("The amount of time that is added depending on distance for a local teleport. This value is in militicks which means that 1000 is one tick and one tick is 1/20 of a second")
                .defineInRange("timeTeleportDist", 10, 0, Integer.MAX_VALUE);

        whooshMessage = SERVER_BUILDER
            .comment("Set this to false to disable the 'whoosh' message on teleport")
            .define("whooshMessage", true);

        teleportVolume = SERVER_BUILDER
                .comment("The volume for the teleporting sound (1.0 is default)")
                .defineInRange("volumeTeleport", 1.0, 0.0, 1.0);
        teleportErrorVolume = SERVER_BUILDER
                .comment("The volume for the error sound when teleportation fails (1.0 is default)")
                .defineInRange("volumeTeleportError", 1.0, 0.0, 1.0);

        matterTransmitterLoadChunk = SERVER_BUILDER
                .comment("The amount of ticks that a matter transmitter with destination checker will wait before checking a receiver in case the chunk is not loaded (-1 to disable this check completely)")
                .defineInRange("checkUnloadedChunk", -1, -1, Integer.MAX_VALUE);
        matterTransmitterLoadWorld = SERVER_BUILDER
                .comment("The amount of ticks that a matter transmitter with destination checker will wait before checking a receiver in case the world is not loaded (-1 to disable this check completely)")
                .defineInRange("checkUnloadedWorld", -1, -1, Integer.MAX_VALUE);

        logTeleportUsages = SERVER_BUILDER
            .comment("If this is true then all usages of the teleport system are logged")
            .define("logTeleportUsages", false);

        preventInterdimensionalTeleports = SERVER_BUILDER
            .comment("If this is true then the RFTools teleportation system cannot be used to travel in the same dimension")
            .define("preventInterdimensionalTeleports", false);

        blacklistedTeleportationSources = SERVER_BUILDER
                .comment("Comma separated list of dimension ids that the teleportation system can't teleport from")
                .define("blacklistedTeleportationSources", "");
        blacklistedTeleportationDestinations = SERVER_BUILDER
                .comment("Comma separated list of dimension ids that the teleportation system can't teleport to")
                .define("blacklistedTeleportationDestinations", "");

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }

    public static Set<Integer> getBlacklistedTeleportationSources() {
        if (blacklistedTeleportationSourcesSet == null) {
            blacklistedTeleportationSourcesSet = new HashSet<>();
            String[] strings = StringUtils.split(blacklistedTeleportationSources.get(), ',');
            for (String string : strings) {
                try {
                    blacklistedTeleportationSourcesSet.add(Integer.parseInt(string));
                } catch (NumberFormatException e) {
                    Logging.logError("Bad formatted 'blacklistedTeleportationSources' config!");
                }
            }
        }
        return blacklistedTeleportationSourcesSet;
    }

    public static Set<Integer> getBlacklistedTeleportationDestinations() {
        if (blacklistedTeleportationDestinationsSet == null) {
            blacklistedTeleportationDestinationsSet = new HashSet<>();
            String[] strings = StringUtils.split(blacklistedTeleportationDestinations.get(), ',');
            for (String string : strings) {
                try {
                    blacklistedTeleportationDestinationsSet.add(Integer.parseInt(string));
                } catch (NumberFormatException e) {
                    Logging.logError("Bad formatted 'blacklistedTeleportationDestinations' config!");
                }
            }
        }
        return blacklistedTeleportationDestinationsSet;
    }
}

package com.mcjty.rftools.blocks.teleporter;

import net.minecraftforge.common.config.Configuration;

public class TeleportConfiguration {
    public static final String CATEGORY_TELEPORTER = "teleporter";

    public static int TRANSMITTER_MAXENERGY = 200000;
    public static int TRANSMITTER_RECEIVEPERTICK = 1000;
    public static int RECEIVER_MAXENERGY = 100000;
    public static int RECEIVER_RECEIVEPERTICK = 500;
    public static int DIALER_MAXENERGY = 50000;
    public static int DIALER_RECEIVEPERTICK = 100;
    public static int horizontalDialerRange = 10;           // Horizontal range the dialing device can check for transmitters
    public static int verticalDialerRange = 5;              // Vertical range the dialing device can check for transmitters
    public static int rfPerDial = 1000;                     // RF Consumed by dialing device when making a new dial
    public static int rfPerCheck = 5000;                    // RF Used to do a check on a receiver.
    public static int rfDialedConnectionPerTick = 10;       // RF Consumed by transmitter when a dial is active and not doing anything else

    public static int CHARGEDPORTER_MAXENERGY = 200000;     // Maximum RF capacity of a charged porter item. Teleporting costs 50% more then normal so keep this into account
    public static int CHARGEDPORTER_RECEIVEPERTICK = 400;

    // The following flags are used to calculate power usage for even starting a teleport. The rfStartTeleportBaseDim (cost of
    // teleporting to another dimension) is also the cap of the local teleport which is calculated by doing
    // rfStartTelelportBaseLocal + dist * rfStartTeleportDist
    public static int rfStartTeleportBaseLocal = 5000;      // Base RF consumed by transmitter when starting a teleport in same dimension
    public static int rfStartTeleportBaseDim = 100000;      // Base RF consumed by transmitter when starting a teleport to another dimension
    public static int rfStartTeleportDist = 10;             // RF per distance unit when starting a teleport
    public static int rfTeleportPerTick = 500;              // During the time the teleport is busy this RF is used per tick on the transmitter
    public static int rfPerTeleportReceiver = 5000;         // On the receiver side we need this amount of power
    public static int rfBoostedTeleport = 20000;            // The RF needed to do a boosted teleportation

    // The following flags are used to calculate the time used for doing the actual teleportation. Same principle as with
    // the power usage above with regards to local/dimensional teleport.
    public static int timeTeleportBaseLocal = 5;
    public static int timeTeleportBaseDim = 50;
    public static int timeTeleportDist = 10;                // Value in militicks (1000 == 1 tick)

    // Base volume for the teleporting sound (whoosh!)
    public static float teleportVolume = 1.0f;
    public static float teleportErrorVolume = 1.0f;

    // Set these flags if you want the matter transmitter to more aggressively check for receiver quality. Possibly with some performance penalty.
    public static int matterTransmitterLoadChunk = -1;
    public static int matterTransmitterLoadWorld = -1;

    public static void init(Configuration cfg) {
        TRANSMITTER_MAXENERGY = cfg.get(CATEGORY_TELEPORTER, "transmitterMaxRF", TRANSMITTER_MAXENERGY,
                "Maximum RF storage that the matter transmitter can hold. This should be at least equal to 'rfStartTeleportDim'").getInt();
        TRANSMITTER_RECEIVEPERTICK = cfg.get(CATEGORY_TELEPORTER, "transmitterRFPerTick", TRANSMITTER_RECEIVEPERTICK,
                "RF per tick that the matter transmitter can receive. It is recommended to keep this at least equal to 'rfTeleportPerTick'").getInt();

        RECEIVER_MAXENERGY = cfg.get(CATEGORY_TELEPORTER, "receiverMaxRF", RECEIVER_MAXENERGY,
                "Maximum RF storage that the matter receiver can hold").getInt();
        RECEIVER_RECEIVEPERTICK = cfg.get(CATEGORY_TELEPORTER, "receiverRFPerTick", RECEIVER_RECEIVEPERTICK,
                "RF per tick that the matter receiver can receive").getInt();

        DIALER_MAXENERGY = cfg.get(CATEGORY_TELEPORTER, "dialerMaxRF", DIALER_MAXENERGY,
                "Maximum RF storage that the dialing device can hold").getInt();
        DIALER_RECEIVEPERTICK = cfg.get(CATEGORY_TELEPORTER, "dialerRFPerTick", DIALER_RECEIVEPERTICK,
                "RF per tick that the dialing device can receive").getInt();

        CHARGEDPORTER_MAXENERGY = cfg.get(CATEGORY_TELEPORTER, "chargedPorterMaxRF", CHARGEDPORTER_MAXENERGY,
                "Maximum RF storage that the charged porter item can hold (note that teleporting this way uses 50% more RF then with a matter transmitter)").getInt();
        CHARGEDPORTER_RECEIVEPERTICK = cfg.get(CATEGORY_TELEPORTER, "chargedPorterRFPerTick", CHARGEDPORTER_RECEIVEPERTICK,
                "RF per tick that the the charged porter item can receive").getInt();

        horizontalDialerRange = cfg.get(CATEGORY_TELEPORTER, "horizontalDialerRange", horizontalDialerRange,
                "The horizontal range the dialing device uses to check for transmitters. These are the transmitters the dialing device will be able to control").getInt();
        verticalDialerRange = cfg.get(CATEGORY_TELEPORTER, "verticalDialerRange", verticalDialerRange,
                "The vertical range the dialing device uses to check for transmitters").getInt();

        rfPerDial = cfg.get(CATEGORY_TELEPORTER, "rfPerDial", rfPerDial,
                "The amount of RF consumed when dialing a transmitter to another receiver").getInt();
        rfPerCheck = cfg.get(CATEGORY_TELEPORTER, "rfPerCheck", rfPerCheck,
                "The amount of RF consumed when the dialing device checks for the capabilities of a receiver ('Check' button)").getInt();
        rfDialedConnectionPerTick = cfg.get(CATEGORY_TELEPORTER, "rfDialedConnectionPerTick", rfDialedConnectionPerTick,
                "The amount of RF that is consumed by the matter transmitter when a dial is active").getInt();

        rfStartTeleportBaseLocal = cfg.get(CATEGORY_TELEPORTER, "rfStartTeleportLocal", rfStartTeleportBaseLocal,
                "The amount of RF that is consumed by a matter transmitter when the player goes to stand in the teleportation beam allowing the teleportation process to start. This value is used for a teleport in the same dimension. In addition to this value the 'rfStartTeleportDist' is also added per traveled distance").getInt();
        rfStartTeleportBaseDim = cfg.get(CATEGORY_TELEPORTER, "rfStartTeleportDim", rfStartTeleportBaseDim,
                "The amount of RF that is consumed by a matter transmitter when the player goes to stand in the teleportation beam allowing the teleportation process to start. This version is for a teleportation to another dimension and in this case 'rfStartTeleportDist' is not used. This value also acts as the maximum rf that can be consumed for a local teleport").getInt();
        rfStartTeleportDist = cfg.get(CATEGORY_TELEPORTER, "rfStartTeleportDist", rfStartTeleportDist,
                "For every unit in distance this value is added to the initial RF cost for starting the teleportation. This value is not used when teleporting to another dimension").getInt();
        rfTeleportPerTick = cfg.get(CATEGORY_TELEPORTER, "rfTeleportPerTick", rfTeleportPerTick,
                "For the duration of the teleport process this value represents the amount of RF that is consumed by the matter transmitter for every tick").getInt();

        rfPerTeleportReceiver = cfg.get(CATEGORY_TELEPORTER, "rfPerTeleportReceiver", rfPerTeleportReceiver,
                "This is the amount of RF that is consumed at the receiving side for every teleport. This RF is only consumed when the teleportation actually happens").getInt();
        rfBoostedTeleport = cfg.get(CATEGORY_TELEPORTER, "rfBoostedTeleport", rfBoostedTeleport,
                "This is the amount of RF that is consumed at a boosted transmitter in case the receiver doesn't have enough power").getInt();

        timeTeleportBaseLocal = cfg.get(CATEGORY_TELEPORTER, "timeTeleportBaseLocal", timeTeleportBaseLocal,
                "The base time used for a teleportation for a local teleport. The 'timeTeleportDist' value is added per distance traveled").getInt();
        timeTeleportBaseDim = cfg.get(CATEGORY_TELEPORTER, "timeTeleportBaseDim", timeTeleportBaseDim,
                "The base time used for a teleportation to another dimension. The 'timeTeleportDist' value is not used").getInt();
        timeTeleportDist = cfg.get(CATEGORY_TELEPORTER, "timeTeleportDist", timeTeleportDist,
                "The amount of time that is added depending on distance for a local teleport. This value is in militicks which means that 1000 is one tick and one tick is 1/20 of a second").getInt();

        teleportVolume = (float) cfg.get(CATEGORY_TELEPORTER, "volumeTeleport", teleportVolume,
                "The volume for the teleporting sound (1.0 is default)").getDouble();
        teleportErrorVolume = (float) cfg.get(CATEGORY_TELEPORTER, "volumeTeleportError", teleportErrorVolume,
                "The volume for the error sound when teleportation fails (1.0 is default)").getDouble();

        matterTransmitterLoadChunk = cfg.get(CATEGORY_TELEPORTER, "checkUnloadedChunk", matterTransmitterLoadChunk,
                "The amount of ticks that a matter transmitter with destination checker will wait before checking a receiver in case the chunk is not loaded (-1 to disable this check completely)").getInt();
        matterTransmitterLoadWorld = cfg.get(CATEGORY_TELEPORTER, "checkUnloadedWorld", matterTransmitterLoadWorld,
                "The amount of ticks that a matter transmitter with destination checker will wait before checking a receiver in case the world is not loaded (-1 to disable this check completely)").getInt();
    }
}

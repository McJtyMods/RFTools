package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.items.teleportprobe.AdvancedChargedPorterItem;
import mcjty.rftools.items.teleportprobe.ChargedPorterItem;
import mcjty.rftools.items.teleportprobe.TeleportProbeItem;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class TeleporterSetup {
    public static MatterTransmitterBlock matterTransmitterBlock;
    public static MatterReceiverBlock matterReceiverBlock;
    public static DialingDeviceBlock dialingDeviceBlock;
    public static DestinationAnalyzerBlock destinationAnalyzerBlock;
    public static MatterBoosterBlock matterBoosterBlock;
    public static SimpleDialerBlock simpleDialerBlock;

    public static TeleportProbeItem teleportProbeItem;
    public static ChargedPorterItem chargedPorterItem;
    public static AdvancedChargedPorterItem advancedChargedPorterItem;

    @ObjectHolder("rftools:matter_receiver")
    public static TileEntityType<?> TYPE_MATTER_RECEIVER;

    @ObjectHolder("rftools:matter_transmitter")
    public static TileEntityType<?> TYPE_MATTER_TRANSMITTER;

    @ObjectHolder("rftools:simple_dialer")
    public static TileEntityType<?> TYPE_SIMPLE_DIALER;

    @ObjectHolder("rftools:dialing_device")
    public static TileEntityType<?> TYPE_DIALING_DEVICE;


    public static void init() {
        matterTransmitterBlock = new MatterTransmitterBlock();
        matterReceiverBlock = new MatterReceiverBlock();
        dialingDeviceBlock = new DialingDeviceBlock();
        simpleDialerBlock = new SimpleDialerBlock();
        destinationAnalyzerBlock = new DestinationAnalyzerBlock();
        matterBoosterBlock = new MatterBoosterBlock();

        teleportProbeItem = new TeleportProbeItem();
        chargedPorterItem = new ChargedPorterItem();
        advancedChargedPorterItem = new AdvancedChargedPorterItem();
    }

//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        matterTransmitterBlock.initModel();
//        matterReceiverBlock.initModel();
//        dialingDeviceBlock.initModel();
//        destinationAnalyzerBlock.initModel();
//        matterBoosterBlock.initModel();
//        simpleDialerBlock.initModel();
//
//        teleportProbeItem.initModel();
//        chargedPorterItem.initModel();
//        advancedChargedPorterItem.initModel();
//    }
}

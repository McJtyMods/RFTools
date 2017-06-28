package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.items.teleportprobe.AdvancedChargedPorterItem;
import mcjty.rftools.items.teleportprobe.ChargedPorterItem;
import mcjty.rftools.items.teleportprobe.TeleportProbeItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        matterTransmitterBlock.initModel();
        matterReceiverBlock.initModel();
        dialingDeviceBlock.initModel();
        destinationAnalyzerBlock.initModel();
        matterBoosterBlock.initModel();
        simpleDialerBlock.initModel();

        teleportProbeItem.initModel();
        chargedPorterItem.initModel();
        advancedChargedPorterItem.initModel();
    }
}

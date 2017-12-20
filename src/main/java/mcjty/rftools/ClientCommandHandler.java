package mcjty.rftools;

import mcjty.lib.McJtyLib;
import mcjty.rftools.blocks.teleporter.MatterTransmitterBlock;

public class ClientCommandHandler {

    public static final String CMD_RETURN_DESTINATION_INFO = "returnDestinationInfo";

    public static void registerCommands() {
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_DESTINATION_INFO, (player, arguments) -> {
            MatterTransmitterBlock.setDestinationInfo(arguments.getInt(), arguments.getString());
            return true;
        });
    }
}

package mcjty.rftools.keys;

import mcjty.lib.debugtools.DumpBlockNBT;
import mcjty.lib.debugtools.DumpItemNBT;
import mcjty.lib.network.Arguments;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.rftools.CommandHandler;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.porterNextDestination.isPressed()) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketSendServerCommand(RFTools.MODID, CommandHandler.CMD_CYCLEDESTINATION,
                    Arguments.builder().value(true).build()));
        } else if (KeyBindings.porterPrevDestination.isPressed()) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketSendServerCommand(RFTools.MODID, CommandHandler.CMD_CYCLEDESTINATION,
                    Arguments.builder().value(false).build()));
        } else if (KeyBindings.debugDumpNBTItem.isPressed()) {
            DumpItemNBT.dumpHeldItem(RFToolsMessages.INSTANCE, Minecraft.getMinecraft().player, false);
        } else if (KeyBindings.debugDumpNBTBlock.isPressed()) {
            DumpBlockNBT.dumpFocusedBlock(RFToolsMessages.INSTANCE, Minecraft.getMinecraft().player, true, false);
        }
    }
}

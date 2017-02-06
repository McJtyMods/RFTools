package mcjty.rftools.keys;

import mcjty.lib.debugtools.DumpBlockNBT;
import mcjty.lib.debugtools.DumpItemNBT;
import mcjty.lib.tools.MinecraftTools;
import mcjty.rftools.items.teleportprobe.PacketCycleDestination;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.porterNextDestination.isPressed()) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketCycleDestination(true));
        } else if (KeyBindings.porterPrevDestination.isPressed()) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketCycleDestination(false));
        } else if (KeyBindings.debugDumpNBTItem.isPressed()) {
            DumpItemNBT.dumpHeldItem(RFToolsMessages.INSTANCE, MinecraftTools.getPlayer(Minecraft.getMinecraft()), false);
        } else if (KeyBindings.debugDumpNBTBlock.isPressed()) {
            DumpBlockNBT.dumpFocusedBlock(RFToolsMessages.INSTANCE, MinecraftTools.getPlayer(Minecraft.getMinecraft()), true, false);
        }
    }
}

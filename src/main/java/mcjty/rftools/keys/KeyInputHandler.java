package mcjty.rftools.keys;

import mcjty.lib.debugtools.DumpBlockNBT;
import mcjty.lib.debugtools.DumpItemNBT;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.CommandHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.debugDumpNBTItem.isPressed()) {
            DumpItemNBT.dumpHeldItem(RFToolsMessages.INSTANCE, Minecraft.getInstance().player, false);
        } else if (KeyBindings.debugDumpNBTBlock.isPressed()) {
            DumpBlockNBT.dumpFocusedBlock(RFToolsMessages.INSTANCE, Minecraft.getInstance().player, true, false);
        }
    }
}

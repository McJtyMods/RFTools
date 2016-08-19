package mcjty.rftools.keys;

import mcjty.rftools.items.teleportprobe.PacketCycleDestination;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.porterNextDestination.isPressed()) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketCycleDestination(true));
        } else if (KeyBindings.porterPrevDestination.isPressed()) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketCycleDestination(false));
        }
    }
}

package mcjty.rftools.keys;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;


import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBindings {

    public static KeyBinding porterNextDestination;
    public static KeyBinding porterPrevDestination;
    public static KeyBinding debugDumpNBTItem;
    public static KeyBinding debugDumpNBTBlock;

    public static void init() {
        porterNextDestination = new KeyBinding("key.porterNextDestination", KeyConflictContext.IN_GAME, Keyboard.KEY_RBRACKET, "key.categories.rftools");
        ClientRegistry.registerKeyBinding(porterNextDestination);
        porterPrevDestination = new KeyBinding("key.porterPrevDestination", KeyConflictContext.IN_GAME, Keyboard.KEY_LBRACKET, "key.categories.rftools");
        ClientRegistry.registerKeyBinding(porterPrevDestination);
        debugDumpNBTItem = new KeyBinding("key.debugDumpNBTItem", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, "key.categories.rftools");
        ClientRegistry.registerKeyBinding(debugDumpNBTItem);
        debugDumpNBTBlock = new KeyBinding("key.debugDumpNBTBlock", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, "key.categories.rftools");
        ClientRegistry.registerKeyBinding(debugDumpNBTBlock);
    }
}

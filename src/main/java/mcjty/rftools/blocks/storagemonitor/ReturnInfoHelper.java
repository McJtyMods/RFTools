package mcjty.rftools.blocks.storagemonitor;



import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ReturnInfoHelper {
    public static void onMessageFromServer(PacketReturnInventoryInfo message) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
                    GuiStorageScanner.fromServer_inventories = message.getInventories();
                });
    }
}

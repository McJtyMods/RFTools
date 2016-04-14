package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GetContentsInfoPacketClient implements InfoPacketClient {

    private List<ItemStack> inv;

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        int size = byteBuf.readInt();
        inv = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            inv.add(NetworkTools.readItemStack(byteBuf));
        }
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(inv.size());
        for (ItemStack stack : inv) {
            NetworkTools.writeItemStack(byteBuf, stack);
        }
    }

    public GetContentsInfoPacketClient() {
    }

    public GetContentsInfoPacketClient(List<ItemStack> inv) {
        this.inv = inv;
    }

    @Override
    public void onMessageClient(EntityPlayerSP entityPlayerSP) {
        GuiStorageScanner.fromServer_inventory = inv;
    }
}

package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class InventoriesInfoPacketClient implements InfoPacketClient {

    private List<BlockPos> inventories;

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        int size = byteBuf.readInt();
        inventories = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            inventories.add(NetworkTools.readPos(byteBuf));
        }
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(inventories.size());
        for (BlockPos pos : inventories) {
            NetworkTools.writePos(byteBuf, pos);
        }

    }

    public InventoriesInfoPacketClient(List<BlockPos> inventories) {
        this.inventories = inventories;
    }

    @Override
    public void onMessageClient(EntityPlayerSP entityPlayerSP) {
        GuiStorageScanner.fromServer_inventories = inventories;
    }
}

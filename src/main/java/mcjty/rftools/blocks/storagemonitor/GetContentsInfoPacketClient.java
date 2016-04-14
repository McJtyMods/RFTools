package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class GetContentsInfoPacketClient implements InfoPacketClient {

    private List<Pair<ItemStack,Integer>> inv;

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        int size = byteBuf.readInt();
        inv = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            inv.add(Pair.of(NetworkTools.readItemStack(byteBuf), byteBuf.readInt()));
        }
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(inv.size());
        for (Pair<ItemStack, Integer> pair : inv) {
            NetworkTools.writeItemStack(byteBuf, pair.getKey());
            byteBuf.writeInt(pair.getValue());
        }
    }

    public GetContentsInfoPacketClient() {
    }

    public GetContentsInfoPacketClient(List<Pair<ItemStack,Integer>> inv) {
        this.inv = inv;
    }

    @Override
    public void onMessageClient(EntityPlayerSP entityPlayerSP) {
        GuiStorageScanner.fromServer_inventory = inv;
    }
}

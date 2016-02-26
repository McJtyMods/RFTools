package mcjty.rftools.blocks.powercell;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class PowerCellInfoPacketClient implements InfoPacketClient {

    private int energy;
    private int blocks;
    private int advancedBlocks;

    public static int tooltipEnergy = 0;
    public static int tooltipBlocks = 0;
    public static int tooltipAdvancedBlocks = 0;

    public PowerCellInfoPacketClient() {
    }

    public PowerCellInfoPacketClient(int energy, int blocks, int advancedBlocks) {
        this.energy = energy;
        this.blocks = blocks;
        this.advancedBlocks = advancedBlocks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        energy = buf.readInt();
        blocks = buf.readInt();
        advancedBlocks = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(energy);
        buf.writeInt(blocks);
        buf.writeInt(advancedBlocks);
    }

    @Override
    public void onMessageClient(EntityPlayerSP player) {
        tooltipEnergy = energy;
        tooltipBlocks = blocks;
        tooltipAdvancedBlocks = advancedBlocks;
    }
}

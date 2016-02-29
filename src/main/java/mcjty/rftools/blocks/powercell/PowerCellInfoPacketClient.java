package mcjty.rftools.blocks.powercell;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class PowerCellInfoPacketClient implements InfoPacketClient {

    private int energy;
    private int blocks;
    private int advancedBlocks;
    private int totalInserted;
    private int totalExtracted;

    public static int tooltipEnergy = 0;
    public static int tooltipBlocks = 0;
    public static int tooltipAdvancedBlocks = 0;
    public static int tooltipInserted = 0;
    public static int tooltipExtracted = 0;

    public PowerCellInfoPacketClient() {
    }

    public PowerCellInfoPacketClient(int energy, int blocks, int advancedBlocks, int totalInserted, int totalExtracted) {
        this.energy = energy;
        this.blocks = blocks;
        this.advancedBlocks = advancedBlocks;
        this.totalInserted = totalInserted;
        this.totalExtracted = totalExtracted;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        energy = buf.readInt();
        blocks = buf.readInt();
        advancedBlocks = buf.readInt();
        totalInserted = buf.readInt();
        totalExtracted = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(energy);
        buf.writeInt(blocks);
        buf.writeInt(advancedBlocks);
        buf.writeInt(totalInserted);
        buf.writeInt(totalExtracted);
    }

    @Override
    public void onMessageClient(EntityPlayerSP player) {
        tooltipEnergy = energy;
        tooltipBlocks = blocks;
        tooltipAdvancedBlocks = advancedBlocks;
        tooltipInserted= totalInserted;
        tooltipExtracted = totalExtracted;
    }
}

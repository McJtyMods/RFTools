package mcjty.rftools.blocks.powercell;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.client.entity.EntityPlayerSP;

public class PowerCellInfoPacketClient implements InfoPacketClient {

    private int energy;
    private int blocks;
    private int simpleBlocks;
    private int advancedBlocks;
    private int totalInserted;
    private int totalExtracted;
    private int rfPerTick;
    private float costFactor;

    public static int tooltipEnergy = 0;
    public static int tooltipBlocks = 0;
    public static int tooltipSimpleBlocks = 0;
    public static int tooltipAdvancedBlocks = 0;
    public static int tooltipInserted = 0;
    public static int tooltipExtracted = 0;
    public static int tooltipRfPerTick = 0;
    public static float tooltipCostFactor = 0;

    public PowerCellInfoPacketClient() {
    }

    public PowerCellInfoPacketClient(int energy, int blocks, int simpleBlocks, int advancedBlocks, int totalInserted, int totalExtracted, int rfPerTick, float costFactor) {
        this.energy = energy;
        this.blocks = blocks;
        this.simpleBlocks = simpleBlocks;
        this.advancedBlocks = advancedBlocks;
        this.totalInserted = totalInserted;
        this.totalExtracted = totalExtracted;
        this.rfPerTick = rfPerTick;
        this.costFactor = costFactor;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        energy = buf.readInt();
        blocks = buf.readInt();
        simpleBlocks = buf.readInt();
        advancedBlocks = buf.readInt();
        totalInserted = buf.readInt();
        totalExtracted = buf.readInt();
        rfPerTick = buf.readInt();
        costFactor = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(energy);
        buf.writeInt(blocks);
        buf.writeInt(simpleBlocks);
        buf.writeInt(advancedBlocks);
        buf.writeInt(totalInserted);
        buf.writeInt(totalExtracted);
        buf.writeInt(rfPerTick);
        buf.writeFloat(costFactor);
    }

    @Override
    public void onMessageClient(EntityPlayerSP player) {
        tooltipEnergy = energy;
        tooltipBlocks = blocks;
        tooltipSimpleBlocks = simpleBlocks;
        tooltipAdvancedBlocks = advancedBlocks;
        tooltipInserted= totalInserted;
        tooltipExtracted = totalExtracted;
        tooltipRfPerTick = rfPerTick;
        tooltipCostFactor = costFactor;
    }
}

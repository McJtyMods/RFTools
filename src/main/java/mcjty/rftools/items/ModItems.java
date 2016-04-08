package mcjty.rftools.items;

import mcjty.rftools.items.creativeonly.ShardWandItem;
import mcjty.rftools.items.manual.RFToolsManualItem;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModItems {

    public static RFToolsManualItem rfToolsManualItem;
    public static SmartWrenchItem smartWrenchItem;
    public static DimensionalShardItem dimensionalShardItem;
    public static ShardWandItem shardWandItem;
    public static InfusedDiamond infusedDiamond;
    public static SyringeItem syringeItem;
    public static PeaceEssenceItem peaceEssenceItem;

    public static void init() {
        setupVariousItems();
    }

    private static void setupVariousItems() {
        smartWrenchItem = new SmartWrenchItem();
        rfToolsManualItem = new RFToolsManualItem();
        dimensionalShardItem = new DimensionalShardItem();
        shardWandItem = new ShardWandItem();
        infusedDiamond = new InfusedDiamond();
        syringeItem = new SyringeItem();
        peaceEssenceItem = new PeaceEssenceItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        smartWrenchItem.initModel();
        rfToolsManualItem.initModel();
        dimensionalShardItem.initModel();
        shardWandItem.initModel();
        infusedDiamond.initModel();
        syringeItem.initModel();
        peaceEssenceItem.initModel();
    }
}

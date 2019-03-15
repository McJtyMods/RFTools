package mcjty.rftools.items;

import mcjty.rftools.items.creativeonly.DevelopersDelightItem;
import mcjty.rftools.items.creativeonly.ShardWandItem;
import mcjty.rftools.items.manual.RFToolsManualItem;
import mcjty.rftools.items.manual.RFToolsShapeManualItem;
import mcjty.rftools.items.modifier.ModifierItem;
import mcjty.rftools.items.netmonitor.NetworkMonitorConfiguration;
import mcjty.rftools.items.netmonitor.NetworkMonitorItem;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModItems {

    public static RFToolsManualItem rfToolsManualItem;
    public static RFToolsShapeManualItem rfToolsShapeManualItem;
    public static SmartWrenchItem smartWrenchItem;
    public static DimensionalShardItem dimensionalShardItem;
    public static ShardWandItem shardWandItem;
    public static InfusedDiamond infusedDiamond;
    public static InfusedEnderpearl infusedEnderpearl;
    public static SyringeItem syringeItem;
    public static PeaceEssenceItem peaceEssenceItem;
    public static DevelopersDelightItem developersDelightItem;
    public static NetworkMonitorItem networkMonitorItem;
    public static ModifierItem modifierItem;

    public static void init() {
        setupVariousItems();
    }

    private static void setupVariousItems() {
        smartWrenchItem = new SmartWrenchItem();
        rfToolsManualItem = new RFToolsManualItem();
        rfToolsShapeManualItem = new RFToolsShapeManualItem();
        dimensionalShardItem = new DimensionalShardItem();
        shardWandItem = new ShardWandItem();
        infusedDiamond = new InfusedDiamond();
        infusedEnderpearl = new InfusedEnderpearl();
        syringeItem = new SyringeItem();
        peaceEssenceItem = new PeaceEssenceItem();
        developersDelightItem = new DevelopersDelightItem();
        if(NetworkMonitorConfiguration.enabled.get())
            networkMonitorItem = new NetworkMonitorItem();
        modifierItem = new ModifierItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        smartWrenchItem.initModel();
        rfToolsManualItem.initModel();
        rfToolsShapeManualItem.initModel();
        dimensionalShardItem.initModel();
        shardWandItem.initModel();
        infusedDiamond.initModel();
        infusedEnderpearl.initModel();
        syringeItem.initModel();
        peaceEssenceItem.initModel();
        developersDelightItem.initModel();
        if(NetworkMonitorConfiguration.enabled.get())
            networkMonitorItem.initModel();
        modifierItem.initModel();
    }
}

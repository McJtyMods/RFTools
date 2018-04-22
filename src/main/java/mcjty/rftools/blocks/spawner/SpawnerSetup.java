package mcjty.rftools.blocks.spawner;

import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpawnerSetup {
    public static GenericBlock<SpawnerTileEntity, GenericContainer> spawnerBlock;
    public static MatterBeamerBlock matterBeamerBlock;

    public static void init() {
        spawnerBlock = ModBlocks.builderFactory.<SpawnerTileEntity> builder("spawner")
                .tileEntityClass(SpawnerTileEntity.class)
                .container(SpawnerTileEntity.CONTAINER_FACTORY)
                .guiId(RFTools.GUI_SPAWNER)
                .moduleSupport(SpawnerTileEntity.MODULE_SUPPORT)
                .information("message.rftools.shiftmessage")
                .informationShift("message.rftools.spawner")
                .build();
        matterBeamerBlock = new MatterBeamerBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        spawnerBlock.initModel();
        spawnerBlock.setGuiClass(GuiSpawner.class);

        matterBeamerBlock.initModel();
    }
}

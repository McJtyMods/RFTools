package mcjty.rftools.blocks.spawner;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.setup.GuiProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpawnerSetup {
    public static BaseBlock<SpawnerTileEntity, GenericContainer> spawnerBlock;
    public static MatterBeamerBlock matterBeamerBlock;

    public static void init() {
        spawnerBlock = ModBlocks.builderFactory.<SpawnerTileEntity> builder("spawner")
                .tileEntityClass(SpawnerTileEntity.class)
                .container(SpawnerTileEntity.CONTAINER_FACTORY)
                .infusable()
                .guiId(GuiProxy.GUI_SPAWNER)
                .moduleSupport(SpawnerTileEntity.MODULE_SUPPORT)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.spawner")
                .build();
        matterBeamerBlock = new MatterBeamerBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        spawnerBlock.initModel();
        spawnerBlock.setGuiFactory(GuiSpawner::new);

        matterBeamerBlock.initModel();
    }
}

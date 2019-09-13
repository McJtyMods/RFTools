package mcjty.rftools.blocks.spawner;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;


public class SpawnerSetup {
    public static BaseBlock spawnerBlock;
    public static MatterBeamerBlock matterBeamerBlock;

    @ObjectHolder("rftools:matter_beamer")
    public static TileEntityType<?> TYPE_MATTER_BEAMER;
    @ObjectHolder("rftools:spawner")
    public static TileEntityType<?> TYPE_SPAWNER;

    public static void init() {
        spawnerBlock = new BaseBlock("spawner", new BlockBuilder()
                .tileEntitySupplier(SpawnerTileEntity::new)
                .infusable()
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.spawner")
        );
        matterBeamerBlock = new MatterBeamerBlock();
    }

//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        spawnerBlock.initModel();
//        spawnerBlock.setGuiFactory(GuiSpawner::new);
//
//        matterBeamerBlock.initModel();
//    }
}

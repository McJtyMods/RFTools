package mcjty.rftools.blocks.spawner;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpawnerSetup {
    public static SpawnerBlock spawnerBlock;
    public static MatterBeamerBlock matterBeamerBlock;

    public static void init() {
        spawnerBlock = new SpawnerBlock();
        matterBeamerBlock = new MatterBeamerBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        spawnerBlock.initModel();
        matterBeamerBlock.initModel();
    }
}

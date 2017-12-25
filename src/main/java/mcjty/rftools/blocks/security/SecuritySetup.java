package mcjty.rftools.blocks.security;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SecuritySetup {
    public static SecurityManagerBlock securityManagerBlock;

    public static SecurityCardItem securityCardItem;
    public static OrphaningCardItem orphaningCardItem;

    public static void init() {
        if(!SecurityConfiguration.enabled) return;
        securityManagerBlock = new SecurityManagerBlock();
        orphaningCardItem = new OrphaningCardItem();
        securityCardItem = new SecurityCardItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        if(!SecurityConfiguration.enabled) return;
        securityManagerBlock.initModel();
        orphaningCardItem.initModel();
        securityCardItem.initModel();
    }
}

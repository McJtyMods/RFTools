package mcjty.rftools.blocks.security;

import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.ItemStackTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static mcjty.lib.varia.ItemStackTools.mapTag;

public class SecuritySetup {
    public static GenericBlock<SecurityManagerTileEntity, GenericContainer> securityManagerBlock;

    public static SecurityCardItem securityCardItem;
    public static OrphaningCardItem orphaningCardItem;

    public static void init() {
        if(!SecurityConfiguration.enabled) return;
        securityManagerBlock = ModBlocks.builderFactory.<SecurityManagerTileEntity> builder("security_manager")
                .tileEntityClass(SecurityManagerTileEntity.class)
                .container(SecurityManagerTileEntity.CONTAINER_FACTORY)
                .guiId(RFTools.GUI_SECURITY_MANAGER)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.security_manager")
                .infoExtendedParameter(stack -> {
                    int cnt = mapTag(stack, compound -> (int) ItemStackTools.getListStream(compound, "Items").filter(nbt -> !new ItemStack((NBTTagCompound)nbt).isEmpty()).count(), 0);
                    return Integer.toString(cnt);
                })
                .build();
        orphaningCardItem = new OrphaningCardItem();
        securityCardItem = new SecurityCardItem();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        if(!SecurityConfiguration.enabled) return;
        securityManagerBlock.initModel();
        securityManagerBlock.setGuiClass(GuiSecurityManager.class);
        orphaningCardItem.initModel();
        securityCardItem.initModel();
    }
}

package mcjty.rftools.blocks.security;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.ItemStackTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.ObjectHolder;

import static mcjty.lib.varia.ItemStackTools.mapTag;

public class SecuritySetup {

    public static final String SECURITY_MANAGER_REGNAME = "security_manager";

    @ObjectHolder("rftools:security_manager")
    public static BaseBlock securityManagerBlock;

    @ObjectHolder("rftools:security_card")
    public static SecurityCardItem securityCardItem;

    @ObjectHolder("rftools:orphaning_card")
    public static OrphaningCardItem orphaningCardItem;

    public static BaseBlock createSecurityManagerBlock() {
        return new BaseBlock(SECURITY_MANAGER_REGNAME, new BlockBuilder()
                .tileEntitySupplier(SecurityManagerTileEntity::new)
                .hasGui()
                .infusable()
                .info("message.rftoolsbase.shiftmessage")
                .infoExtended("message.rftools.security_manager"))
                // @todo 1.14
//                .infoExtendedParameter(stack -> {
//                    int cnt = mapTag(stack, compound -> (int) ItemStackTools.getListStream(compound, "Items").filter(nbt -> !new ItemStack((CompoundNBT)nbt).isEmpty()).count(), 0);
//                    return Integer.toString(cnt);
//                })
                ;
    }

    public static void init() {
        if(!SecurityConfiguration.enabled.get()) return;
        securityManagerBlock = ModBlocks.builderFactory.<SecurityManagerTileEntity> builder("security_manager")
                .tileEntityClass(SecurityManagerTileEntity.class)
                .container(SecurityManagerTileEntity.CONTAINER_FACTORY)
                .guiId(GuiProxy.GUI_SECURITY_MANAGER)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.security_manager")
                .infoExtendedParameter(stack -> {
                    int cnt = mapTag(stack, compound -> (int) ItemStackTools.getListStream(compound, "Items").filter(nbt -> !new ItemStack((CompoundNBT)nbt).isEmpty()).count(), 0);
                    return Integer.toString(cnt);
                })
                .build();
        orphaningCardItem = new OrphaningCardItem();
        securityCardItem = new SecurityCardItem();
    }

//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        if(!SecurityConfiguration.enabled.get()) return;
//        securityManagerBlock.initModel();
//        securityManagerBlock.setGuiFactory(GuiSecurityManager::new);
//        orphaningCardItem.initModel();
//        securityCardItem.initModel();
//    }
}

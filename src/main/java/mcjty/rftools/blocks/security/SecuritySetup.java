package mcjty.rftools.blocks.security;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.ItemStackTools;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
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


    @ObjectHolder("rftools:security_manager")
    public static TileEntityType<?> TYPE_SECURITY_MANAGER;

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
        securityManagerBlock = new BaseBlock("security_manager", new BlockBuilder()
                .tileEntitySupplier(SecurityManagerTileEntity::new)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.security_manager")
                .infoExtendedParameter(stack -> {
                    int cnt = mapTag(stack, compound -> (int) ItemStackTools.getListStream(compound, "Items").filter(nbt -> !ItemStack.read((CompoundNBT)nbt).isEmpty()).count(), 0);
                    return Integer.toString(cnt);
                }));
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

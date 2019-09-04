package mcjty.rftools.blocks.crafter;

import mcjty.lib.McJtyLib;
import mcjty.lib.api.Infusable;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;


//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class CrafterBlock extends GenericRFToolsBlock implements Infusable, INBTPreservingIngredient
        /*, IRedstoneConnectable*/ {

    public CrafterBlock(String blockName, Supplier<TileEntity> tileEntitySupplier) {
        super(blockName, new BlockBuilder()
            .tileEntitySupplier(tileEntitySupplier));
    }

//    @SideOnly(Side.CLIENT)
//    @Override
//    public BiFunction<CrafterBaseTE, CrafterContainer, GenericGuiContainer<? super CrafterBaseTE>> getGuiFactory() {
//        return GuiCrafter::new;
//    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            ListNBT bufferTagList = tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);
            ListNBT recipeTagList = tagCompound.getList("Recipes", Constants.NBT.TAG_COMPOUND);

            int rc = 0;
            for (int i = 0 ; i < bufferTagList.size() ; i++) {
                CompoundNBT itemTag = bufferTagList.getCompound(i);
                if (itemTag != null) {
                    ItemStack stack = ItemStack.read(itemTag);
                    if (!stack.isEmpty()) {
                        rc++;
                    }
                }
            }

            list.add(new StringTextComponent(TextFormatting.GREEN + "Contents: " + rc + " stacks"));

            rc = 0;
            for (int i = 0 ; i < recipeTagList.size() ; i++) {
                CompoundNBT tagRecipe = recipeTagList.getCompound(i);
                CompoundNBT resultCompound = tagRecipe.getCompound("Result");
                if (resultCompound != null) {
                    ItemStack stack = ItemStack.read(resultCompound);
                    if (!stack.isEmpty()) {
                        rc++;
                    }
                }
            }

            list.add(new StringTextComponent(TextFormatting.GREEN + "Recipes: " + rc + " recipes"));
        }

        if (McJtyLib.proxy.isShiftKeyDown()) {
            int amount = 2;
            // @todo 1.14 find another way!
//            if (tileEntityClass.equals(CrafterBlockTileEntity1.class)) {
//                amount = 2;
//            } else if (tileEntityClass.equals(CrafterBlockTileEntity2.class)) {
//                amount = 4;
//            } else {
//                amount = 8;
//            }
            list.add(new StringTextComponent(TextFormatting.WHITE + "This machine can handle up to " + amount + " recipes"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "at once and allows recipes to use the crafting results"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "of previous steps."));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption."));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }

    // @todo 1.14
//    @Override
//    protected IModuleSupport getModuleSupport() {
//        return new ModuleSupport(CrafterContainer.SLOT_FILTER_MODULE) {
//            @Override
//            public boolean isModule(ItemStack itemStack) {
//                return itemStack.getItem() == ModularStorageSetup.storageFilterItem;
//            }
//        };
//    }

//    @Override
//    public Container createServerContainer(PlayerEntity PlayerEntity, TileEntity tileEntity) {
//        CrafterBaseTE crafterBaseTE = (CrafterBaseTE) tileEntity;
//        crafterBaseTE.getInventoryHelper().setStackInSlot(CrafterContainer.SLOT_CRAFTOUTPUT, ItemStack.EMPTY);
//        for (int i = CrafterContainer.SLOT_CRAFTINPUT ; i < CrafterContainer.SLOT_CRAFTINPUT + 9 ; i++) {
//            crafterBaseTE.getInventoryHelper().setStackInSlot(i, ItemStack.EMPTY);
//        }
//        return super.createServerContainer(PlayerEntity, tileEntity);
//    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, Direction from) {
//        return true;
//    }
//

    // @todo 1.14 implement me
    @Override
    public Collection<String> getTagsToPreserve() {
        return null;
    }
}

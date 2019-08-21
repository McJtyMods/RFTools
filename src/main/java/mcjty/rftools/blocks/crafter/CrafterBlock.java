package mcjty.rftools.blocks.crafter;

import mcjty.lib.api.IModuleSupport;
import mcjty.lib.api.Infusable;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.varia.ModuleSupport;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;


import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.BiFunction;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class CrafterBlock extends GenericRFToolsBlock<CrafterBaseTE, CrafterContainer> implements Infusable, INBTPreservingIngredient
        /*, IRedstoneConnectable*/ {

    public CrafterBlock(String blockName, Class<? extends CrafterBaseTE> tileEntityClass) {
        super(Material.IRON, tileEntityClass, CrafterContainer::new, blockName, true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<CrafterBaseTE, CrafterContainer, GenericGuiContainer<? super CrafterBaseTE>> getGuiFactory() {
        return GuiCrafter::new;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<ITextComponent> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            ListNBT bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            ListNBT recipeTagList = tagCompound.getTagList("Recipes", Constants.NBT.TAG_COMPOUND);

            int rc = 0;
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                CompoundNBT itemTag = bufferTagList.getCompoundTagAt(i);
                if (itemTag != null) {
                    ItemStack stack = new ItemStack(itemTag);
                    if (!stack.isEmpty()) {
                        rc++;
                    }
                }
            }

            list.add(TextFormatting.GREEN + "Contents: " + rc + " stacks");

            rc = 0;
            for (int i = 0 ; i < recipeTagList.tagCount() ; i++) {
                CompoundNBT tagRecipe = recipeTagList.getCompoundTagAt(i);
                CompoundNBT resultCompound = tagRecipe.getCompoundTag("Result");
                if (resultCompound != null) {
                    ItemStack stack = new ItemStack(resultCompound);
                    if (!stack.isEmpty()) {
                        rc++;
                    }
                }
            }

            list.add(TextFormatting.GREEN + "Recipes: " + rc + " recipes");
        }

        if (McJtyLib.proxy.isShiftKeyDown()) {
            int amount;
            if (tileEntityClass.equals(CrafterBlockTileEntity1.class)) {
                amount = 2;
            } else if (tileEntityClass.equals(CrafterBlockTileEntity2.class)) {
                amount = 4;
            } else {
                amount = 8;
            }
            list.add(TextFormatting.WHITE + "This machine can handle up to " + amount + " recipes");
            list.add(TextFormatting.WHITE + "at once and allows recipes to use the crafting results");
            list.add(TextFormatting.WHITE + "of previous steps.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    @Override
    protected IModuleSupport getModuleSupport() {
        return new ModuleSupport(CrafterContainer.SLOT_FILTER_MODULE) {
            @Override
            public boolean isModule(ItemStack itemStack) {
                return itemStack.getItem() == ModularStorageSetup.storageFilterItem;
            }
        };
    }

    @Override
    public Container createServerContainer(PlayerEntity PlayerEntity, TileEntity tileEntity) {
        CrafterBaseTE crafterBaseTE = (CrafterBaseTE) tileEntity;
        crafterBaseTE.getInventoryHelper().setStackInSlot(CrafterContainer.SLOT_CRAFTOUTPUT, ItemStack.EMPTY);
        for (int i = CrafterContainer.SLOT_CRAFTINPUT ; i < CrafterContainer.SLOT_CRAFTINPUT + 9 ; i++) {
            crafterBaseTE.getInventoryHelper().setStackInSlot(i, ItemStack.EMPTY);
        }
        return super.createServerContainer(PlayerEntity, tileEntity);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, Direction from) {
//        return true;
//    }
//
    @Override
    public int getGuiID() {
        return GuiProxy.GUI_CRAFTER;
    }
}

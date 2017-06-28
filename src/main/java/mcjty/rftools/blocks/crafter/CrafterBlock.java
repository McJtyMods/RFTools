package mcjty.rftools.blocks.crafter;

import mcjty.lib.api.IModuleSupport;
import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.varia.ModuleSupport;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.crafting.INBTPreservingIngredient;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class CrafterBlock extends GenericRFToolsBlock<CrafterBaseTE, CrafterContainer> implements Infusable, INBTPreservingIngredient
        /*, IRedstoneConnectable*/ {

    public CrafterBlock(String blockName, Class<? extends CrafterBaseTE> tileEntityClass) {
        super(Material.IRON, tileEntityClass, CrafterContainer.class, blockName, true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiCrafter.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            NBTTagList recipeTagList = tagCompound.getTagList("Recipes", Constants.NBT.TAG_COMPOUND);

            int rc = 0;
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                NBTTagCompound itemTag = bufferTagList.getCompoundTagAt(i);
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
                NBTTagCompound tagRecipe = recipeTagList.getCompoundTagAt(i);
                NBTTagCompound resultCompound = tagRecipe.getCompoundTag("Result");
                if (resultCompound != null) {
                    ItemStack stack = new ItemStack(resultCompound);
                    if (!stack.isEmpty()) {
                        rc++;
                    }
                }
            }

            list.add(TextFormatting.GREEN + "Recipes: " + rc + " recipes");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
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
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
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
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        CrafterBaseTE crafterBaseTE = (CrafterBaseTE) tileEntity;
        crafterBaseTE.getInventoryHelper().setStackInSlot(CrafterContainer.SLOT_CRAFTOUTPUT, ItemStack.EMPTY);
        for (int i = CrafterContainer.SLOT_CRAFTINPUT ; i < CrafterContainer.SLOT_CRAFTINPUT + 9 ; i++) {
            crafterBaseTE.getInventoryHelper().setStackInSlot(i, ItemStack.EMPTY);
        }
        return super.createServerContainer(entityPlayer, tileEntity);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, EnumFacing from) {
//        return true;
//    }
//
    @Override
    public int getGuiID() {
        return RFTools.GUI_CRAFTER;
    }
}

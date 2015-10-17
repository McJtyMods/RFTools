package mcjty.rftools.blocks.endergen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class PearlInjectorBlock extends GenericRFToolsBlock {

    public PearlInjectorBlock() {
        super(Material.iron, PearlInjectorTileEntity.class, true);
        setBlockName("pearlInjectorBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public String getIdentifyingIconName() {
        return "machinePearlInjector";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);

            int rc = 0;
            for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
                NBTTagCompound itemTag = bufferTagList.getCompoundTagAt(i);
                if (itemTag != null) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
                    if (stack != null) {
                        rc++;
                    }
                }
            }

            list.add(EnumChatFormatting.GREEN + "Contents: " + rc + " stacks");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This will inject an ender pearl in an adjacent");
            list.add(EnumChatFormatting.WHITE + "endergenic generator when a redstone signal is");
            list.add(EnumChatFormatting.WHITE + "received.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_PEARL_INJECTOR;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        PearlInjectorTileEntity pearlInjectorTileEntity = (PearlInjectorTileEntity) tileEntity;
        PearlInjectorContainer pearlInjectorContainer = new PearlInjectorContainer(entityPlayer, pearlInjectorTileEntity);
        return new GuiPearlInjector(pearlInjectorTileEntity, pearlInjectorContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new PearlInjectorContainer(entityPlayer, (PearlInjectorTileEntity) tileEntity);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

}

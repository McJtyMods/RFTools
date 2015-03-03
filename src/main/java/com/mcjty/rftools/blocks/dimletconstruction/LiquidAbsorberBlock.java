package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class LiquidAbsorberBlock extends GenericBlock {

    public LiquidAbsorberBlock() {
        super(Material.iron, LiquidAbsorberTileEntity.class);
        setBlockName("liquidAbsorberBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            int blockID = tagCompound.getInteger("liquid");
            if (blockID != -1) {
                Block block = (Block) Block.blockRegistry.getObjectById(blockID);
                if (block != null) {
                    Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
                    currenttip.add(EnumChatFormatting.GREEN + "Liquid: " + new FluidStack(fluid, 1).getLocalizedName());
                    int absorbing = tagCompound.getInteger("absorbing");
                    int pct = ((DimletConstructionConfiguration.maxLiquidAbsorbtion - absorbing) * 100) / DimletConstructionConfiguration.maxLiquidAbsorbtion;
                    currenttip.add(EnumChatFormatting.GREEN + "Absorbed: " + pct + "%");
                }
            }
        }
        return currenttip;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int blockID = tagCompound.getInteger("liquid");
            if (blockID != -1) {
                Block block = (Block) Block.blockRegistry.getObjectById(blockID);
                if (block != null) {
                    Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
                    list.add(EnumChatFormatting.GREEN + "Liquid: " + new FluidStack(fluid, 1).getLocalizedName());
                    int absorbing = tagCompound.getInteger("absorbing");
                    int pct = ((DimletConstructionConfiguration.maxLiquidAbsorbtion - absorbing) * 100) / DimletConstructionConfiguration.maxLiquidAbsorbtion;
                    list.add(EnumChatFormatting.GREEN + "Absorbed: " + pct + "%");
                }
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Place this block on top of a liquid and it will");
            list.add(EnumChatFormatting.WHITE + "gradually absorb all this liquid in the area.");
            list.add(EnumChatFormatting.WHITE + "You can use the end result in the Dimlet Workbench.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public String getSideIconName() {
        return "liquidAbsorber";
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return 0;
    }

    @Override
    public int getGuiID() {
        return -1;
    }
}

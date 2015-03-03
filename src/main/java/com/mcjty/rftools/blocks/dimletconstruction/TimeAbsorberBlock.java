package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class TimeAbsorberBlock extends GenericBlock {

    public TimeAbsorberBlock() {
        super(Material.iron, TimeAbsorberTileEntity.class);
        setBlockName("timeAbsorberBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }



    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof TimeAbsorberTileEntity) {
            TimeAbsorberTileEntity timeAbsorberTileEntity = (TimeAbsorberTileEntity) te;
            float angle = timeAbsorberTileEntity.getAngle();
            if (angle >= -0.01f) {
                int id = TimeAbsorberTileEntity.findBestTimeDimlet(angle);
                String name = KnownDimletConfiguration.idToDisplayName.get(id);
                if (name == null) {
                    name = "<unknown>";
                }
                currenttip.add(EnumChatFormatting.GREEN + "Dimlet: " + name + " (" + angle + ")");
                int absorbing = timeAbsorberTileEntity.getAbsorbing();
                int pct = ((DimletConstructionConfiguration.maxTimeAbsorbtion - absorbing) * 100) / DimletConstructionConfiguration.maxTimeAbsorbtion;
                currenttip.add(EnumChatFormatting.GREEN + "Absorbed: " + pct + "%");
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
            if (tagCompound.hasKey("angle") && tagCompound.getFloat("angle") > -0.001f) {
                float angle = tagCompound.getFloat("angle");
                int id = TimeAbsorberTileEntity.findBestTimeDimlet(angle);
                String name = KnownDimletConfiguration.idToDisplayName.get(id);
                if (name == null) {
                    name = "<unknown>";
                }
                list.add(EnumChatFormatting.GREEN + "Dimlet: " + name + " (" + angle + ")");
                int absorbing = tagCompound.getInteger("absorbing");
                int pct = ((DimletConstructionConfiguration.maxTimeAbsorbtion - absorbing) * 100) / DimletConstructionConfiguration.maxTimeAbsorbtion;
                list.add(EnumChatFormatting.GREEN + "Absorbed: " + pct + "%");
                int timeout = tagCompound.getInteger("registerTimeout");
                list.add(EnumChatFormatting.GREEN + "Timeout: " + timeout);
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Place this block outside and give it a redstone");
            list.add(EnumChatFormatting.WHITE + "signal around the time that you want to absorb.");
            list.add(EnumChatFormatting.WHITE + "You can use the end result in the Dimlet Workbench.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public String getSideIconName() {
        return "timeAbsorber";
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

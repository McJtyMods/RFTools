package mcjty.rftools.blocks.dimletconstruction;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.api.redstone.IRedstoneConnectable;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

@Optional.InterfaceList({
        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class TimeAbsorberBlock extends GenericRFToolsBlock implements IRedstoneConnectable {

    public TimeAbsorberBlock() {
        super(Material.iron, TimeAbsorberTileEntity.class, false);
        setBlockName("timeAbsorberBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

    @Override
    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, ForgeDirection from) {
        return true;
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
                DimletKey key = TimeAbsorberTileEntity.findBestTimeDimlet(angle);
                String name = KnownDimletConfiguration.idToDisplayName.get(key);
                if (name == null) {
                    name = "<unknown>";
                }
                int absorbing = timeAbsorberTileEntity.getAbsorbing();
                int pct = ((DimletConstructionConfiguration.maxTimeAbsorbtion - absorbing) * 100) / DimletConstructionConfiguration.maxTimeAbsorbtion;
                currenttip.add(EnumChatFormatting.GREEN + "Dimlet: " + name + " (" + angle + ", " + pct + "%)");
            } else {
                currenttip.add(EnumChatFormatting.GREEN + "Give this block a redstone signal");
                currenttip.add(EnumChatFormatting.GREEN + "at the right time you want to absorb");
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
                DimletKey key = TimeAbsorberTileEntity.findBestTimeDimlet(angle);
                String name = KnownDimletConfiguration.idToDisplayName.get(key);
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
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + getSideIconName());
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

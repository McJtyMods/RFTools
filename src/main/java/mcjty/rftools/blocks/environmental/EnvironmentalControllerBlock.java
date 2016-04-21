package mcjty.rftools.blocks.environmental;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.varia.RFToolsTools;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class EnvironmentalControllerBlock extends GenericRFToolsBlock implements Infusable /*, IRedstoneConnectable*/ {

    public EnvironmentalControllerBlock() {
        super(Material.iron, EnvironmentalControllerTileEntity.class, EnvironmentalControllerContainer.class, "environmental_controller", true);
    }

    @Override
    public boolean hasNoRotation() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        super.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(EnvironmentalControllerTileEntity.class, new EnvironmentalTESR());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiEnvironmentalController.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            EnvironmentalControllerTileEntity tileEntity = (EnvironmentalControllerTileEntity) accessor.getTileEntity();
            int rfPerTick = tileEntity.getTotalRfPerTick();
            int volume = tileEntity.getVolume();
            if (tileEntity.isActive()) {
                currenttip.add(TextFormatting.GREEN + "Active " + rfPerTick + " RF/tick (" + volume + " blocks)");
            } else {
                currenttip.add(TextFormatting.GREEN + "Inactive (" + volume + " blocks)");
            }
            int radius = tileEntity.getRadius();
            int miny = tileEntity.getMiny();
            int maxy = tileEntity.getMaxy();
            currenttip.add(TextFormatting.GREEN + "Area: radius " + radius + " (between " + miny + " and " + maxy + ")");
        }
        return currenttip;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int radius = tagCompound.getInteger("radius");
            int miny = tagCompound.getInteger("miny");
            int maxy = tagCompound.getInteger("maxy");
            list.add(TextFormatting.GREEN + "Area: radius " + radius + " (between " + miny + " and " + maxy + ")");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Control the environment around you with various");
            list.add(TextFormatting.WHITE + "modules. There are modules for things like regeneration,");
            list.add(TextFormatting.WHITE + "speed...");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (heldItem != null && heldItem.getItem() instanceof EnvModuleProvider) {
            if (RFToolsTools.installModule(player, heldItem, hand, pos, EnvironmentalControllerContainer.SLOT_MODULES, EnvironmentalControllerContainer.SLOT_MODULES + EnvironmentalControllerContainer.ENV_MODULES)) {
                return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        checkRedstoneWithTE(world, pos);
    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, ForgeDirection from) {
//        return true;
//    }


    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 13;
//        int meta = world.getBlockMetadata(x, y, z);
//        int state = BlockTools.getState(meta);
//        if (state == 0) {
//            return 10;
//        } else {
//            return getLightValue();
//        }
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_ENVIRONMENTAL_CONTROLLER;
    }
}

package mcjty.rftools.blocks.endergen;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.Achievements;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
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
public class EndergenicBlock extends GenericRFToolsBlock<EndergenicTileEntity, EmptyContainer> implements Infusable /*, IRedstoneConnectable*/ {

    public EndergenicBlock() {
        super(Material.IRON, EndergenicTileEntity.class, EmptyContainer.class, "endergenic", true);
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

    @Override
    public boolean hasNoRotation() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        super.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(EndergenicTileEntity.class, new EndergenicRenderer());
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERGENIC;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiEndergenic.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Generate power out of ender pearls. You need at");
            list.add(TextFormatting.WHITE + "least two generators for this to work and the setup");
            list.add(TextFormatting.WHITE + "is relatively complicated. Timing is crucial.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: increased power generation and");
            list.add(TextFormatting.YELLOW + "reduced powerloss for holding pearls.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        if (mode == ProbeMode.EXTENDED) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof EndergenicTileEntity) {
                EndergenicTileEntity tileEntity = (EndergenicTileEntity) te;
                IItemStyle style = probeInfo.defaultItemStyle().width(16).height(13);
                ILayoutStyle layoutStyle = probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER);
                probeInfo.text(TextFormatting.BLUE + "Stats over the last 5 seconds:");
                probeInfo.horizontal(layoutStyle)
                        .item(new ItemStack(Items.REDSTONE), style)
                        .text("Charged " + tileEntity.getLastChargeCounter() + " time(s)");
                probeInfo.horizontal(layoutStyle)
                        .item(new ItemStack(Items.ENDER_PEARL), style)
                        .text("Fired " + tileEntity.getLastPearlsLaunched())
                        .text(" / Lost " + tileEntity.getLastPearlsLost());
                if (tileEntity.getLastPearlsLost() > 0) {
                    probeInfo.text(TextFormatting.RED + tileEntity.getLastPearlsLostReason());
                }
                if (tileEntity.getLastPearlArrivedAt() > -2) {
                    probeInfo.text("Last pearl arrived at " + tileEntity.getLastPearlArrivedAt());
                }
                probeInfo.horizontal()
                    .text(TextFormatting.GREEN + "RF Gain " + tileEntity.getLastRfGained())
                    .text(" / ")
                    .text(TextFormatting.RED + "Lost " + tileEntity.getLastRfLost())
                    .text(" (RF/t " + tileEntity.getLastRfPerTick() + ")");
            }
        } else {
            probeInfo.text("(sneak to get statistics)");
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof EntityPlayer) {
            Achievements.trigger((EntityPlayer) placer, Achievements.hardPower);
        }
    }

    @Override
    protected boolean wrenchUse(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        if (world.isRemote) {
            EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) world.getTileEntity(pos);
            SoundEvent pling = SoundEvent.REGISTRY.getObject(new ResourceLocation("block.note.pling"));
            world.playSound(player, pos, pling, SoundCategory.BLOCKS, 1.0f, 1.0f);
            endergenicTileEntity.useWrench(player);
        }
        return true;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        System.out.println("EndergenicBlock.getActualState");
        return super.getActualState(state, worldIn, pos);
    }
//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, ForgeDirection from) {
//        return true;
//    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

}

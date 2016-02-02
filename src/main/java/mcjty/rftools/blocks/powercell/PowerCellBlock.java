package mcjty.rftools.blocks.powercell;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.network.clientinfo.PacketGetInfoFromServer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.network.RFToolsMessages;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.text.DecimalFormat;
import java.util.List;

public class PowerCellBlock extends GenericRFToolsBlock<PowerCellTileEntity, PowerCellContainer> {

    // Properties that indicate if there is the same block in a certain direction.
//    public static final PropertyBool NORTH = PropertyBool.create("north");
//    public static final PropertyBool SOUTH = PropertyBool.create("south");
//    public static final PropertyBool WEST = PropertyBool.create("west");
//    public static final PropertyBool EAST = PropertyBool.create("east");
//    public static final PropertyBool UP = PropertyBool.create("up");
//    public static final PropertyBool DOWN = PropertyBool.create("down");

    private static long lastTime = 0;

    public PowerCellBlock() {
        super(Material.iron, PowerCellTileEntity.class, PowerCellContainer.class, "powercell", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiPowerCell.class;
    }

    @Override
    public boolean hasNoRotation() {
        return true;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_POWERCELL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.YELLOW + "Energy: " + tagCompound.getInteger("energy"));
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add("Part of a powercell multi-block.");
            list.add("You can place these in any configuration.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity tileEntity = accessor.getTileEntity();
        if (tileEntity instanceof PowerCellTileEntity) {
            PowerCellTileEntity powerCellTileEntity = (PowerCellTileEntity) tileEntity;
            int id = powerCellTileEntity.getNetworkId();
            if (id != -1) {
                currenttip.add(EnumChatFormatting.GREEN + "ID: " + new DecimalFormat("#.##").format(id));
                if (System.currentTimeMillis() - lastTime > 250) {
                    lastTime = System.currentTimeMillis();
                    RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID, new PowerCellInfoPacketServer(id)));
                }
                currenttip.add(EnumChatFormatting.GREEN + "Energy: " + PowerCellInfoPacketClient.tooltipEnergy + "/" + (PowerCellInfoPacketClient.tooltipBlocks * PowerCellConfiguration.rfPerCell) + " RF");
            } else {
                currenttip.add(EnumChatFormatting.YELLOW + "No powercard!");
            }
        }
        return currenttip;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            world.markBlockRangeForRenderUpdate(pos.add(-1, -1, -1), pos.add(1, 1, 1));
        }
        if (stack.hasTagCompound() && !world.isRemote) {
            PowerCellTileEntity powerCellTileEntity = (PowerCellTileEntity) world.getTileEntity(pos);
            if (powerCellTileEntity != null) {
                int networkId = powerCellTileEntity.getNetworkId();
                if (networkId == -1) {
                    // No network, energy is already restored to the local block
                } else {
                    int energy = stack.getTagCompound().getInteger("energy");
                    PowerCellNetwork powerCellNetwork = PowerCellNetwork.getChannels(world);
                    PowerCellNetwork.Network channel = powerCellNetwork.getChannel(networkId);
                    channel.setEnergy(energy + channel.getEnergy());
                    powerCellNetwork.save(world);
                }
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess blockAccess, BlockPos pos, IBlockState state, int fortune) {
        World world = (World) blockAccess;
        List<ItemStack> drops = super.getDrops(world, pos, state, fortune);
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof PowerCellTileEntity) {
                PowerCellNetwork.Network network = ((PowerCellTileEntity) te).getNetwork();
                if (network != null) {
                    int energy = network.getEnergy() / Math.max(1, network.getBlocks().size());
                    if (!drops.isEmpty()) {
                        NBTTagCompound tagCompound = drops.get(0).getTagCompound();
                        if (tagCompound == null) {
                            tagCompound = new NBTTagCompound();
                            drops.get(0).setTagCompound(tagCompound);
                        }
                        tagCompound.setInteger("energy", energy);
                    }
                }
            }
        }
        return drops;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof PowerCellTileEntity) {
                PowerCellNetwork.Network network = ((PowerCellTileEntity) te).getNetwork();
                if (network != null) {
                    int energy = network.getEnergy() / network.getBlocks().size();
                    network.setEnergy(network.getEnergy() - energy);
                }
            }
        }
        super.breakBlock(world, pos, state);
    }
}

package mcjty.rftools.blocks.teleporter;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MatterReceiverBlock extends GenericRFToolsBlock implements Infusable {

    public MatterReceiverBlock() {
        super(Material.iron, MatterReceiverTileEntity.class, EmptyContainer.class, "matter_receiver", false);
        setCreativeTab(RFTools.tabRfTools);
        setDefaultState(this.blockState.getBaseState());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiMatterReceiver.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String name = tagCompound.getString("tpName");
            int id = tagCompound.getInteger("destinationId");
            list.add(TextFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "If you place this block anywhere in the world then");
            list.add(TextFormatting.WHITE + "you can dial to it using a Dialing Device. Before");
            list.add(TextFormatting.WHITE + "teleporting to this block make sure to give it power!");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    //@todo
//    @SideOnly(Side.CLIENT)
//    @Override
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.getWailaBody(itemStack, currenttip, accessor, config);
//        TileEntity te = accessor.getTileEntity();
//        if (te instanceof MatterReceiverTileEntity) {
//            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
//            String name = matterReceiverTileEntity.getName();
//            int id = matterReceiverTileEntity.getId();
//            currenttip.add(TextFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
//        }
//        return currenttip;
//    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MATTER_RECEIVER;
    }

    @Override
    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float sx, float sy, float sz, int meta, EntityLivingBase placer) {
        IBlockState state = super.onBlockPlaced(world, pos, facing, sx, sy, sz, meta, placer);
        if (world.isRemote) {
            return state;
        }
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);

        GlobalCoordinate gc = new GlobalCoordinate(pos, world.provider.getDimension());

        destinations.getNewId(gc);
        destinations.addDestination(gc);
        destinations.save(world);

        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        // We don't want what GenericBlock does.
        // This is called AFTER onBlockPlaced below. Here we need to fix the destination settings.
        restoreBlockFromNBT(world, pos, stack);
        if (!world.isRemote) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) world.getTileEntity(pos);
            matterReceiverTileEntity.getOrCalculateID();
            matterReceiverTileEntity.updateDestination();
        }
        setOwner(world, pos, placer);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        if (world.isRemote) {
            return;
        }
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        destinations.removeDestination(pos, world.provider.getDimension());
        destinations.save(world);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this);
    }
}

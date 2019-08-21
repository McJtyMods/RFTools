package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.screenmodules.ButtonModuleItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class RedstoneChannelBlock<T extends RedstoneChannelTileEntity, C extends Container> extends LogicSlabBlock<T, C> {

    public RedstoneChannelBlock(Material material, Class<? extends T> tileEntityClass, BiFunction<EntityPlayer, IInventory, C> containerFactory, Function<Block, ItemBlock> itemBlockFactory, String name) {
        super(RFTools.instance, material, tileEntityClass, containerFactory, itemBlockFactory, name, false);
        setCreativeTab(RFTools.setup.getTab());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        CompoundNBT tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int channel = tagCompound.getInteger("channel");
            list.add(TextFormatting.GREEN + "Channel: " + channel);
        }
    }

    private boolean isRedstoneChannelItem(Item item) {
        return (item instanceof ItemBlock && ((ItemBlock)item).getBlock() instanceof RedstoneChannelBlock) || item instanceof ButtonModuleItem;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if(isRedstoneChannelItem(stack.getItem())) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof RedstoneChannelTileEntity) {
                if(!world.isRemote) {
                    RedstoneChannelTileEntity rcte = (RedstoneChannelTileEntity)te;
                    CompoundNBT tagCompound = stack.getTagCompound();
                    if (tagCompound == null) {
                        tagCompound = new CompoundNBT();
                        stack.setTagCompound(tagCompound);
                    }
                    int channel;
                    if(!player.isSneaking()) {
                        channel = rcte.getChannel(true);
                        tagCompound.setInteger("channel", channel);
                    } else {
                        if (tagCompound.hasKey("channel")) {
                            channel = tagCompound.getInteger("channel");
                        } else {
                            channel = -1;
                        }
                        if(channel == -1) {
                            RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
                            channel = redstoneChannels.newChannel();
                            redstoneChannels.save();
                            tagCompound.setInteger("channel", channel);
                        }
                        rcte.setChannel(channel);
                    }
                    Logging.message(player, TextFormatting.YELLOW + "Channel set to " + channel + "!");
                }
                return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }
}

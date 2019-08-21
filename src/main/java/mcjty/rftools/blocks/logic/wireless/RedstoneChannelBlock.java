package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.Logging;
import mcjty.rftools.items.screenmodules.ButtonModuleItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public abstract class RedstoneChannelBlock extends LogicSlabBlock {

    public RedstoneChannelBlock(String name, BlockBuilder builder) {
        super(name, builder);
//        setCreativeTab(RFTools.setup.getTab());
    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            int channel = tagCompound.getInt("channel");
            list.add(new StringTextComponent(TextFormatting.GREEN + "Channel: " + channel));
        }
    }

    private boolean isRedstoneChannelItem(Item item) {
        return (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof RedstoneChannelBlock) || item instanceof ButtonModuleItem;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        ItemStack stack = player.getHeldItem(hand);
        if(isRedstoneChannelItem(stack.getItem())) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof RedstoneChannelTileEntity) {
                if(!world.isRemote) {
                    RedstoneChannelTileEntity rcte = (RedstoneChannelTileEntity)te;
                    CompoundNBT tagCompound = stack.getOrCreateTag();
                    int channel;
                    if(!player.isSneaking()) {
                        channel = rcte.getChannel(true);
                        tagCompound.putInt("channel", channel);
                    } else {
                        if (tagCompound.contains("channel")) {
                            channel = tagCompound.getInt("channel");
                        } else {
                            channel = -1;
                        }
                        if(channel == -1) {
                            RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
                            channel = redstoneChannels.newChannel();
                            redstoneChannels.save();
                            tagCompound.putInt("channel", channel);
                        }
                        rcte.setChannel(channel);
                    }
                    Logging.message(player, TextFormatting.YELLOW + "Channel set to " + channel + "!");
                }
                return true;
            }
        }
        return super.onBlockActivated(state, world, pos, player, hand, result);
    }
}

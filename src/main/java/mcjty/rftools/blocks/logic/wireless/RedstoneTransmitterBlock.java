package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.McJtyLib;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class RedstoneTransmitterBlock extends RedstoneChannelBlock {

    public RedstoneTransmitterBlock() {
        super("redstone_transmitter_block", new BlockBuilder()
            .tileEntitySupplier(RedstoneTransmitterTileEntity::new));
    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This logic block accepts redstone signals and"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "sends them out wirelessly to linked receivers"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "Place down to create a channel or else right"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "click on receiver/transmitter to use that channel"));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_220069_6_) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, p_220069_6_);
        RedstoneTransmitterTileEntity te = (RedstoneTransmitterTileEntity) world.getTileEntity(pos);
        te.update();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            // @todo double check
            ((RedstoneTransmitterTileEntity)world.getTileEntity(pos)).update();
        }
    }
}

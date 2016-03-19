package mcjty.rftools.blocks.logic;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ThreeLogicBlock extends LogicSlabBlock<ThreeLogicTileEntity, EmptyContainer> {

    public ThreeLogicBlock() {
        super(Material.iron, "logic_block", ThreeLogicTileEntity.class, EmptyContainer.class);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiThreeLogic.class;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block can do various logical");
            list.add(TextFormatting.WHITE + "operations on three inputs.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }

    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof LogicTileEntity) {
            LogicTileEntity logicTileEntity = (LogicTileEntity)te;
            int powered1 = getInputStrength(world, pos, logicTileEntity.getFacing().getOutputSide().rotateY()) > 0 ? 1 : 0;
            int powered2 = getInputStrength(world, pos, logicTileEntity.getFacing().getOutputSide()) > 0 ? 2 : 0;
            int powered3 = getInputStrength(world, pos, logicTileEntity.getFacing().getOutputSide().rotateYCCW()) > 0 ? 4 : 0;
            logicTileEntity.setPowered(powered1 + powered2 + powered3);
        }
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_THREE_LOGIC;
    }
}

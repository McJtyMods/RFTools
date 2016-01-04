package mcjty.rftools.blocks.screens;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class ScreenHitBlock extends Block implements ITileEntityProvider {
    public ScreenHitBlock() {
        super(Material.glass);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setUnlocalizedName("screen_hitblock");
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new ScreenHitTileEntity();
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new ScreenHitTileEntity();
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer playerIn) {
        if (world.isRemote) {
            ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(pos);
            int dx = screenHitTileEntity.getDx();
            int dy = screenHitTileEntity.getDy();
            int dz = screenHitTileEntity.getDz();
            Block block = world.getBlockState(pos.add(dx, dy, dz)).getBlock();
            if (block != ScreenSetup.screenBlock) {
                return;
            }


            MovingObjectPosition mouseOver = Minecraft.getMinecraft().objectMouseOver;
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(pos.add(dx, dy, dz));
            screenTileEntity.hitScreenClient(mouseOver.hitVec.xCoord - pos.getX() - dx, mouseOver.hitVec.yCoord - pos.getY() - dy, mouseOver.hitVec.zCoord - pos.getZ() - dz, mouseOver.sideHit);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float sidex, float sidey, float sidez) {
        ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(pos);
        int dx = screenHitTileEntity.getDx();
        int dy = screenHitTileEntity.getDy();
        int dz = screenHitTileEntity.getDz();
        Block block = world.getBlockState(pos.add(dx, dy, dz)).getBlock();
        if (block != ScreenSetup.screenBlock) {
            return false;
        }
        return block.onBlockActivated(world, pos.add(dx, dy, dz), state, player, side, sidex, sidey, sidez);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        int meta = state.getBlock().getMetaFromState(state);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        if (meta == 2) {
            this.setBlockBounds(0.0F, 0.0F, 1.0F - 0.125F, 1.0F, 1.0F, 1.0F);
        }

        if (meta == 3) {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.125F);
        }

        if (meta == 4) {
            this.setBlockBounds(1.0F - 0.125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }

        if (meta == 5) {
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.125F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;              // Invisible
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public int getMobilityFlag() {
        return 2;
    }
}

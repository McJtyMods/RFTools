package mcjty.rftools.blocks.screens;

import mcjty.rftools.network.Argument;
import mcjty.rftools.network.PacketHandler;
import mcjty.rftools.network.PacketServerCommand;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
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
        setBlockName("screenHitBlock");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new ScreenHitTileEntity();
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) {
            ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(x, y, z);
            int dx = screenHitTileEntity.getDx();
            int dy = screenHitTileEntity.getDy();
            int dz = screenHitTileEntity.getDz();
            Block block = world.getBlock(x + dx, y + dy, z + dz);
            if (block != ScreenSetup.screenBlock) {
                return;
            }


            MovingObjectPosition mouseOver = Minecraft.getMinecraft().objectMouseOver;
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(x+dx, y+dy, z+dz);
            screenTileEntity.hitScreenClient(mouseOver.hitVec.xCoord - x - dx, mouseOver.hitVec.yCoord - y - dy, mouseOver.hitVec.zCoord - z - dz, mouseOver.sideHit);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sidex, float sidey, float sidez) {
        ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(x, y, z);
        int dx = screenHitTileEntity.getDx();
        int dy = screenHitTileEntity.getDy();
        int dz = screenHitTileEntity.getDz();
        Block block = world.getBlock(x + dx, y + dy, z + dz);
        if (block != ScreenSetup.screenBlock) {
            return false;
        }
        return block.onBlockActivated(world, x + dx, y + dy, z + dz, player, side, sidex, sidey, sidez);
    }

    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
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
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        return false;
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
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

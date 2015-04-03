package mcjty.rftools.blocks.spaceprojector;

import mcjty.rftools.RFTools;
import mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.Random;

public class ProxyBlock extends Block implements ITileEntityProvider {

    public static int RENDERID_PROXYBLOCK;

    public ProxyBlock() {
        super(Material.glass);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setBlockName("proxyBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return RENDERID_PROXYBLOCK;
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

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sidex, float sidey, float sidez) {
        if (!world.isRemote) {
            ProxyBlockTileEntity proxyBlockTileEntity = (ProxyBlockTileEntity) world.getTileEntity(x, y, z);
            Coordinate oc = proxyBlockTileEntity.getOrigCoordinate();
            int dim = proxyBlockTileEntity.getDimension();
            World w = DimensionManager.getWorld(dim);
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
            entityPlayerMP.theItemInWorldManager.activateBlockOrUseItem(player, w, null, oc.getX(), oc.getY(), oc.getZ(), side, sidex, sidey, sidez);
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new ProxyBlockTileEntity();
    }
}

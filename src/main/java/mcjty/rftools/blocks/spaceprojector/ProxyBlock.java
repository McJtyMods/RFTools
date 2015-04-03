package mcjty.rftools.blocks.spaceprojector;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

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
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new ProxyBlockTileEntity();
    }
}

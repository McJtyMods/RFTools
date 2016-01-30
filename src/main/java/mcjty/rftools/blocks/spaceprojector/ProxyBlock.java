package mcjty.rftools.blocks.spaceprojector;

import com.mojang.authlib.GameProfile;
import mcjty.lib.varia.Coordinate;
import mcjty.rftools.RFTools;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Random;
import java.util.UUID;

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
            final Coordinate oc = proxyBlockTileEntity.getOrigCoordinate();
            int dim = proxyBlockTileEntity.getDimension();
            final WorldServer w = DimensionManager.getWorld(dim);

            final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;

//            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(w);
            FakePlayer fakePlayer = new FakePlayer(w, new GameProfile(UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77"), "[Minecraft]")) {
                @Override
                public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
                    entityPlayerMP.openGui(mod, modGuiId, w, oc.getX(), oc.getY(), oc.getZ());
                }
            };
            fakePlayer.getPlayerCoordinates().posX = oc.getX();
            fakePlayer.getPlayerCoordinates().posY = oc.getY();
            fakePlayer.getPlayerCoordinates().posZ = oc.getZ();
            fakePlayer.playerNetServerHandler = entityPlayerMP.playerNetServerHandler;
            fakePlayer.inventory = entityPlayerMP.inventory;
            fakePlayer.dimension = dim;

//            World oldWorld = entityPlayerMP.worldObj;
//            int oldPosX = entityPlayerMP.getPlayerCoordinates().posX;
//            int oldPosY = entityPlayerMP.getPlayerCoordinates().posY;
//            int oldPosZ = entityPlayerMP.getPlayerCoordinates().posZ;
//            entityPlayerMP.worldObj = w;
//            entityPlayerMP.getPlayerCoordinates().posX = oc.getX();
//            entityPlayerMP.getPlayerCoordinates().posY = oc.getY();
//            entityPlayerMP.getPlayerCoordinates().posZ = oc.getZ();

            entityPlayerMP.theItemInWorldManager.activateBlockOrUseItem(fakePlayer, w, null, oc.getX(), oc.getY(), oc.getZ(), side, sidex, sidey, sidez);


//            entityPlayerMP.worldObj = oldWorld;
//            entityPlayerMP.getPlayerCoordinates().posX = oldPosX;
//            entityPlayerMP.getPlayerCoordinates().posY = oldPosY;
//            entityPlayerMP.getPlayerCoordinates().posZ = oldPosZ;

        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new ProxyBlockTileEntity();
    }
}

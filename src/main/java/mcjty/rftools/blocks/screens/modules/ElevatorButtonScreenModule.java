package mcjty.rftools.blocks.screens.modules;

import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleDataContents;
import mcjty.rftools.blocks.elevator.ElevatorTileEntity;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import static mcjty.rftools.blocks.screens.modulesclient.ElevatorButtonClientScreenModule.LARGESIZE;
import static mcjty.rftools.blocks.screens.modulesclient.ElevatorButtonClientScreenModule.SMALLSIZE;

public class ElevatorButtonScreenModule implements IScreenModule<IModuleDataContents> {
    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    protected ScreenModuleHelper helper = new ScreenModuleHelper();
    private boolean vertical = false;
    private boolean large = false;

    @Override
    public IModuleDataContents getData(IScreenDataHelper helper, World worldObj, long millis) {
        World world = DimensionManager.getWorld(dim);
        if (world == null) {
            return null;
        }

        if (!RFToolsTools.chunkLoaded(world, coordinate)) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate);

        if (!(te instanceof ElevatorTileEntity)) {
            return null;
        }

        ElevatorTileEntity elevatorTileEntity = (ElevatorTileEntity) te;
        return helper.createContents(elevatorTileEntity.getCurrentLevel(), elevatorTileEntity.getLevelCount(), 0);
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            coordinate = BlockPosTools.INVALID;
            if (tagCompound.hasKey("elevatorx")) {
                if (tagCompound.hasKey("elevatordim")) {
                    this.dim = tagCompound.getInteger("elevatordim");
                } else {
                    // Compatibility reasons
                    this.dim = tagCompound.getInteger("dim");
                }
                if (dim == this.dim) {
                    BlockPos c = new BlockPos(tagCompound.getInteger("elevatorx"), tagCompound.getInteger("elevatory"), tagCompound.getInteger("elevatorz"));
                    int dx = Math.abs(c.getX() - pos.getX());
                    int dy = Math.abs(c.getY() - pos.getY());
                    int dz = Math.abs(c.getZ() - pos.getZ());
                    if (dx <= 64 && dy <= 64 && dz <= 64) {
                        coordinate = c;
                    }
                }
                vertical = tagCompound.getBoolean("vertical");
                large = tagCompound.getBoolean("large");
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, EntityPlayer player) {
        if (BlockPosTools.INVALID == coordinate) {
            return;
        }
        World w = DimensionManager.getWorld(dim);
        if (w == null) {
            return;
        }

        if (!RFToolsTools.chunkLoaded(world, coordinate)) {
            return;
        }

        TileEntity te = w.getTileEntity(coordinate);
        if (!(te instanceof ElevatorTileEntity)) {
            return;
        }
        ElevatorTileEntity elevatorTileEntity = (ElevatorTileEntity) te;

        if (vertical) {
            int yoffset = 0;
            if (y >= yoffset) {
                int level = (y - yoffset) / (large ? (LARGESIZE - 2) : (SMALLSIZE - 2));
                if (level < 0) {
                    return;
                }
                elevatorTileEntity.toLevel(((ElevatorTileEntity) te).getLevelCount() - level - 1);
            }
        } else {
            int xoffset = 5;
            if (x >= xoffset) {
                int level = (x - xoffset) / (large ? (LARGESIZE - 2) : (SMALLSIZE - 2));
                if (level < 0) {
                    return;
                }
                elevatorTileEntity.toLevel(level);
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.ELEVATOR_BUTTON_RFPERTICK;
    }
}

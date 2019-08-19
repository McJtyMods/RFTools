package mcjty.rftools.blocks.screens.modules;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.blocks.elevator.ElevatorTileEntity;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

import static mcjty.rftools.blocks.screens.modulesclient.ElevatorButtonClientScreenModule.LARGESIZE;
import static mcjty.rftools.blocks.screens.modulesclient.ElevatorButtonClientScreenModule.SMALLSIZE;

public class ElevatorButtonScreenModule implements IScreenModule<ElevatorButtonScreenModule.ModuleElevatorInfo> {
    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    protected ScreenModuleHelper helper = new ScreenModuleHelper();
    private boolean vertical = false;
    private boolean large = false;

    public static class ModuleElevatorInfo implements IModuleData {

        public static final String ID = RFTools.MODID + ":elevator";

        private int level;
        private int maxLevel;
        private BlockPos pos;
        private List<Integer> heights;

        @Override
        public String getId() {
            return ID;
        }

        public ModuleElevatorInfo(int level, int maxLevel, BlockPos pos, List<Integer> heights) {
            this.level = level;
            this.maxLevel = maxLevel;
            this.pos = pos;
            this.heights = heights;
        }

        public ModuleElevatorInfo(ByteBuf buf) {
            level = buf.readInt();
            maxLevel = buf.readInt();
            pos = NetworkTools.readPos(buf);
            int s = buf.readByte();
            heights = new ArrayList<>(s);
            for (int i = 0; i < s; i++) {
                heights.add((int) buf.readShort());
            }
        }

        public BlockPos getPos() {
            return pos;
        }

        public List<Integer> getHeights() {
            return heights;
        }

        public int getLevel() {
            return level;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        @Override
        public void writeToBuf(ByteBuf buf) {
            buf.writeInt(level);
            buf.writeInt(maxLevel);
            NetworkTools.writePos(buf, pos);
            buf.writeByte(heights.size());
            for (Integer height : heights) {
                buf.writeShort(height);
            }
        }
    }

    @Override
    public ModuleElevatorInfo getData(IScreenDataHelper helper, World worldObj, long millis) {
        World world = DimensionManager.getWorld(dim);
        if (world == null) {
            return null;
        }

        if (!WorldTools.chunkLoaded(world, coordinate)) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate);

        if (!(te instanceof ElevatorTileEntity)) {
            return null;
        }

        ElevatorTileEntity elevatorTileEntity = (ElevatorTileEntity) te;
        List<Integer> heights = new ArrayList<>();
        elevatorTileEntity.findElevatorBlocks(heights);
        return new ModuleElevatorInfo(elevatorTileEntity.getCurrentLevel(heights),
                elevatorTileEntity.getLevelCount(heights),
                elevatorTileEntity.findBottomElevator(),
                heights);
    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
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
                    int dz = Math.abs(c.getZ() - pos.getZ());
                    if (dx <= 64 && dz <= 64) {
                        coordinate = c;
                    }
                }
                vertical = tagCompound.getBoolean("vertical");
                large = tagCompound.getBoolean("large");
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked, PlayerEntity player) {
        if (BlockPosTools.INVALID.equals(coordinate)) {
            if (player != null) {
                player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Module is not linked to elevator!"), false);
            }
            return;
        }
        World w = DimensionManager.getWorld(dim);
        if (w == null) {
            return;
        }

        if (!WorldTools.chunkLoaded(world, coordinate)) {
            return;
        }

        TileEntity te = w.getTileEntity(coordinate);
        if (!(te instanceof ElevatorTileEntity)) {
            return;
        }
        ElevatorTileEntity elevatorTileEntity = (ElevatorTileEntity) te;

        List<Integer> heights = new ArrayList<>();
        elevatorTileEntity.findElevatorBlocks(heights);
        int levelCount = elevatorTileEntity.getLevelCount(heights);
        int level = -1;

        if (vertical) {
            int max = large ? 6 : 8;
            int numcols = (levelCount + max - 1) / max;
            int colw = getColumnWidth(numcols);

            int yoffset = 0;
            if (y >= yoffset) {
                level = (y - yoffset) / (((large ? LARGESIZE : SMALLSIZE) - 2));
                if (level < 0) {
                    return;
                }
                if (numcols > 1) {
                    int col = (x - 5) / (colw + 7);
                    level = max - level - 1 + col * max;
                    if (col == numcols - 1) {
                        level -= max - (levelCount % max);
                    }
                } else {
                    level = levelCount - level - 1;
                }
            }
        } else {
            int xoffset = 5;
            if (x >= xoffset) {
                level = (x - xoffset) / (((large ? LARGESIZE : SMALLSIZE) - 2));
            }
        }
        if (level >= 0 && level < levelCount) {
            elevatorTileEntity.toLevel(level);
        }
    }

    public static int getColumnWidth(int numcols) {
        int colw;
        switch (numcols) {
            case 1: colw = 120; break;
            case 2: colw = 58; break;
            case 3: colw = 36; break;
            case 4: colw = 25; break;
            case 5: colw = 19; break;
            default: colw = 15; break;
        }
        return colw;
    }


    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.ELEVATOR_BUTTON_RFPERTICK.get();
    }
}

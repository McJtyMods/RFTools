package mcjty.rftools.blocks.screens.modules;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.tools.ChatTools;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IScreenDataHelper;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.blocks.elevator.ElevatorTileEntity;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
                heights.add((int) buf.readByte());
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
                buf.writeByte(height);
            }
        }
    }

    @Override
    public ModuleElevatorInfo getData(IScreenDataHelper helper, World worldObj, long millis) {
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
        List<Integer> heights = new ArrayList<>();
        elevatorTileEntity.findElevatorBlocks(heights);
        return new ModuleElevatorInfo(elevatorTileEntity.getCurrentLevel(heights),
                elevatorTileEntity.getLevelCount(heights),
                elevatorTileEntity.findBottomElevator(),
                heights);
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
    public void mouseClick(World world, int x, int y, boolean clicked, EntityPlayer player) {
        if (BlockPosTools.INVALID.equals(coordinate)) {
            if (player != null) {
                ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "Module is not linked to elevator!"));
            }
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

        List<Integer> heights = new ArrayList<>();
        elevatorTileEntity.findElevatorBlocks(heights);
        int levelCount = elevatorTileEntity.getLevelCount(heights);
        int level = -1;

        if (vertical) {
            int max = large ? 6 : 8;
            boolean twocols = levelCount > max;

            int yoffset = 0;
            if (y >= yoffset) {
                level = (y - yoffset) / (((large ? LARGESIZE : SMALLSIZE) - 2));
                if (level < 0) {
                    return;
                }
                if (twocols) {
                    if (x > 73) {
                        if (level < levelCount - max) {
                            level = levelCount - level - 1;
                        } else {
                            level = -1;
                        }
                    } else {
                        level = max - level - 1;
                    }
                } else {
                    level = levelCount - level - 1;
                }
                System.out.println("level = " + level);
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

    @Override
    public int getRfPerTick() {
        return ScreenConfiguration.ELEVATOR_BUTTON_RFPERTICK;
    }
}

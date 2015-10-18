package mcjty.rftools.commands;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

public class CmdRCC extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "rcc";
    }

    @Override
    public int getPermissionLevel() {
        return 1;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        World world = sender.getEntityWorld();
        IChunkProvider chunkProvider = world.getChunkProvider();
        if (chunkProvider instanceof ChunkProviderServer) {
            ChunkProviderServer chunkProviderServer = (ChunkProviderServer) chunkProvider;
            for (Object o : chunkProviderServer.loadedChunks) {
                Chunk chunk = (Chunk) o;
                replaceBricks(world, chunk, 0, 0);
                replaceBricks(world, chunk, 15, 0);
                replaceBricks(world, chunk, 15, 15);
                replaceBricks(world, chunk, 0, 15);
            }
        }
    }

    private void replaceBricks(World world, Chunk chunk, int x, int y) {
        Block block = world.getBlock((chunk.xPosition << 4) + x, 128, (chunk.zPosition << 4) + y);
        if (block == Blocks.brick_block) {
            world.setBlockToAir((chunk.xPosition << 4) + x, 128, (chunk.zPosition << 4) + y);
        }
    }
}

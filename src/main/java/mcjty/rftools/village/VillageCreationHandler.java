package mcjty.rftools.village;

import cpw.mods.fml.common.registry.VillagerRegistry;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.List;
import java.util.Random;

public class VillageCreationHandler implements VillagerRegistry.IVillageCreationHandler {
    @Override
    public Object buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List pieces, Random random, int p1, int p2, int p3, int p4, int p5) {
        return VillagePiece.buildPiece(startPiece, pieces, random, p1, p2, p3, p4, p5);
    }

    @Override
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int i) {
        return new StructureVillagePieces.PieceWeight(VillagePiece.class, 4, 1);
    }

    @Override
    public Class<?> getComponentClass() {
        return VillagePiece.class;
    }
}

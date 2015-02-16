package com.mcjty.rftools.blocks.shield;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.renderer.Tessellator;

import java.lang.reflect.Field;

public class TesseleratorAccessHelper {
    private static Field addedVerticesField;

    private static void initializeAddedVerticiesField() {
        if (addedVerticesField == null) {
            addedVerticesField = ReflectionHelper.findField(Tessellator.class, "field_78411_s", "addedVertices");
        }
    }

    public static int getAddedVertices(Tessellator tesselerator) {
        initializeAddedVerticiesField();
        try {
            return addedVerticesField.getInt(tesselerator);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

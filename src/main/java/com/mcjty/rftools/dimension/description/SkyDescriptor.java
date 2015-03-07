package com.mcjty.rftools.dimension.description;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.world.types.CelestialBodyType;
import com.mcjty.rftools.dimension.world.types.SkyType;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class SkyDescriptor {
    private final Float sunBrightnessFactor;
    private final Float starBrightnessFactor;

    private final Float skyColorFactorR;
    private final Float skyColorFactorG;
    private final Float skyColorFactorB;

    private final Float fogColorFactorR;
    private final Float fogColorFactorG;
    private final Float fogColorFactorB;

    private final SkyType skyType;

    private final List<CelestialBodyType> celestialBodies;

    private SkyDescriptor(Builder builder) {
        sunBrightnessFactor = builder.sunBrightnessFactor;
        starBrightnessFactor = builder.starBrightnessFactor;
        skyColorFactorR = builder.skyColorFactorR;
        skyColorFactorG = builder.skyColorFactorG;
        skyColorFactorB = builder.skyColorFactorB;
        fogColorFactorR = builder.fogColorFactorR;
        fogColorFactorG = builder.fogColorFactorG;
        fogColorFactorB = builder.fogColorFactorB;
        skyType = builder.skyType;
        celestialBodies = new ArrayList<CelestialBodyType>(builder.celestialBodies);
    }

    public void toBytes(ByteBuf buf) {
        writeFloat(buf, sunBrightnessFactor);
        writeFloat(buf, starBrightnessFactor);
        writeFloat(buf, skyColorFactorR);
        writeFloat(buf, skyColorFactorG);
        writeFloat(buf, skyColorFactorB);
        writeFloat(buf, fogColorFactorR);
        writeFloat(buf, fogColorFactorG);
        writeFloat(buf, fogColorFactorB);
        writeInteger(buf, skyType == null ? null : skyType.ordinal());
        buf.writeInt(celestialBodies.size());
        for (CelestialBodyType body : celestialBodies) {
            buf.writeInt(body.ordinal());
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        if (sunBrightnessFactor != null) {
            compound.setFloat("sunBrightness", sunBrightnessFactor);
        }
        if (starBrightnessFactor != null) {
            compound.setFloat("starBrightness", starBrightnessFactor);
        }
        if (skyColorFactorR != null) {
            compound.setFloat("skyColorFactorR", skyColorFactorR);
        }
        if (skyColorFactorG != null) {
            compound.setFloat("skyColorFactorG", skyColorFactorG);
        }
        if (skyColorFactorB != null) {
            compound.setFloat("skyColorFactorB", skyColorFactorB);
        }
        if (fogColorFactorR != null) {
            compound.setFloat("fogColorFactorR", fogColorFactorR);
        }
        if (fogColorFactorG != null) {
            compound.setFloat("fogColorFactorG", fogColorFactorG);
        }
        if (fogColorFactorB != null) {
            compound.setFloat("fogColorFactorB", fogColorFactorB);
        }
        if (skyType != null) {
            compound.setInteger("skyType", skyType.ordinal());
        }

        int[] bodies = new int[celestialBodies.size()];
        for (int i = 0 ; i < celestialBodies.size() ; i++) {
            bodies[i] = celestialBodies.get(i).ordinal();
        }
        compound.setIntArray("celestialBodies", bodies);
    }

    private void writeFloat(ByteBuf buf, Float value) {
        if (value == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeFloat(value);
        }
    }

    private void writeInteger(ByteBuf buf, Integer value) {
        if (value == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeInt(value);
        }
    }

    public float getSunBrightnessFactor() {
        return sunBrightnessFactor == null ? 1.0f : sunBrightnessFactor;
    }

    public float getStarBrightnessFactor() {
        return starBrightnessFactor == null ? 1.0f : starBrightnessFactor;
    }

    public float getSkyColorFactorR() {
        return skyColorFactorR == null ? 1.0f : skyColorFactorR;
    }

    public float getSkyColorFactorG() {
        return skyColorFactorG == null ? 1.0f : skyColorFactorG;
    }

    public float getSkyColorFactorB() {
        return skyColorFactorB == null ? 1.0f : skyColorFactorB;
    }

    public float getFogColorFactorR() {
        return fogColorFactorR == null ? 1.0f : fogColorFactorR;
    }

    public float getFogColorFactorG() {
        return fogColorFactorG == null ? 1.0f : fogColorFactorG;
    }

    public float getFogColorFactorB() {
        return fogColorFactorB == null ? 1.0f : fogColorFactorB;
    }

    public SkyType getSkyType() {
        return skyType == null ? SkyType.SKY_NORMAL : skyType;
    }

    public boolean specifiesSkyColor() {
        return skyColorFactorR != null;
    }

    public boolean specifiesFogColor() {
        return fogColorFactorR != null;
    }

    public List<CelestialBodyType> getCelestialBodies() {
        return celestialBodies;
    }

    public static class Builder {
        private Float sunBrightnessFactor = null;
        private Float starBrightnessFactor = null;
        private Float skyColorFactorR = null;
        private Float skyColorFactorG = null;
        private Float skyColorFactorB = null;
        private Float fogColorFactorR = null;
        private Float fogColorFactorG = null;
        private Float fogColorFactorB = null;
        private SkyType skyType;
        private List<CelestialBodyType> celestialBodies = new ArrayList<CelestialBodyType>();

        public Builder fromBytes(ByteBuf buf) {
            sunBrightnessFactor = readFloat(buf);
            starBrightnessFactor = readFloat(buf);
            skyColorFactorR = readFloat(buf);
            skyColorFactorG = readFloat(buf);
            skyColorFactorB = readFloat(buf);
            fogColorFactorR = readFloat(buf);
            fogColorFactorG = readFloat(buf);
            fogColorFactorB = readFloat(buf);
            Integer skyTypeI = readInteger(buf);
            if (skyTypeI == null) {
                skyType = null;
            } else {
                skyType = SkyType.values()[skyTypeI];
            }
            celestialBodies.clear();
            int size = buf.readInt();
            for (int i = 0 ; i < size ; i++) {
                celestialBodies.add(CelestialBodyType.values()[buf.readInt()]);
            }
            return this;
        }

        public Builder fromNBT(NBTTagCompound compound) {
            if (compound.hasKey("sunBrightness")) {
                sunBrightnessFactor = compound.getFloat("sunBrightness");
            } else {
                sunBrightnessFactor = null;
            }
            if (compound.hasKey("starBrightness")) {
                starBrightnessFactor = compound.getFloat("starBrightness");
            } else {
                starBrightnessFactor = null;
            }
            if (compound.hasKey("skyColorFactorR")) {
                skyColorFactorR = compound.getFloat("skyColorFactorR");
            } else {
                skyColorFactorR = null;
            }
            if (compound.hasKey("skyColorFactorG")) {
                skyColorFactorG = compound.getFloat("skyColorFactorG");
            } else {
                skyColorFactorG = null;
            }
            if (compound.hasKey("skyColorFactorB")) {
                skyColorFactorB = compound.getFloat("skyColorFactorB");
            } else {
                skyColorFactorB = null;
            }
            if (compound.hasKey("fogColorFactorR")) {
                fogColorFactorR = compound.getFloat("fogColorFactorR");
            } else {
                fogColorFactorR = null;
            }
            if (compound.hasKey("fogColorFactorG")) {
                fogColorFactorG = compound.getFloat("fogColorFactorG");
            } else {
                fogColorFactorG = null;
            }
            if (compound.hasKey("fogColorFactorB")) {
                fogColorFactorB = compound.getFloat("fogColorFactorB");
            } else {
                fogColorFactorB = null;
            }

            if (compound.hasKey("skyType")) {
                int skyTypeI = compound.getInteger("skyType");
                skyType = SkyType.values()[skyTypeI];
            } else {
                skyType = null;
            }

            int[] bodies = DimensionInformation.getIntArraySafe(compound, "celestialBodies");
            celestialBodies.clear();
            for (int body : bodies) {
                celestialBodies.add(CelestialBodyType.values()[body]);
            }

            return this;
        }

        private Float readFloat(ByteBuf buf) {
            if (buf.readBoolean()) {
                return buf.readFloat();
            } else {
                return null;
            }
        }

        private Integer readInteger(ByteBuf buf) {
            if (buf.readBoolean()) {
                return buf.readInt();
            } else {
                return null;
            }
        }

        public Builder combine(SkyDescriptor descriptor) {
            if (descriptor.starBrightnessFactor != null) {
                starBrightnessFactor(descriptor.getStarBrightnessFactor());
            }
            if (descriptor.sunBrightnessFactor != null) {
                sunBrightnessFactor(descriptor.getSunBrightnessFactor());
            }
            if (descriptor.skyType != null) {
                skyType(descriptor.skyType);
            }
            if (descriptor.skyColorFactorR != null) {
                if (this.skyColorFactorR == null) {
                    this.skyColorFactorR = descriptor.skyColorFactorR;
                } else {
                    this.skyColorFactorR *= descriptor.skyColorFactorR;
                }
            }
            if (descriptor.skyColorFactorG != null) {
                if (this.skyColorFactorG == null) {
                    this.skyColorFactorG = descriptor.skyColorFactorG;
                } else {
                    this.skyColorFactorG *= descriptor.skyColorFactorG;
                }
            }
            if (descriptor.skyColorFactorB != null) {
                if (this.skyColorFactorB == null) {
                    this.skyColorFactorB = descriptor.skyColorFactorB;
                } else {
                    this.skyColorFactorB *= descriptor.skyColorFactorB;
                }
            }
            if (descriptor.fogColorFactorR != null) {
                if (this.fogColorFactorR == null) {
                    this.fogColorFactorR = descriptor.fogColorFactorR;
                } else {
                    this.fogColorFactorR *= descriptor.fogColorFactorR;
                }
            }
            if (descriptor.fogColorFactorG != null) {
                if (this.fogColorFactorG == null) {
                    this.fogColorFactorG = descriptor.fogColorFactorG;
                } else {
                    this.fogColorFactorG *= descriptor.fogColorFactorG;
                }
            }
            if (descriptor.fogColorFactorB != null) {
                if (this.fogColorFactorB == null) {
                    this.fogColorFactorB = descriptor.fogColorFactorB;
                } else {
                    this.fogColorFactorB *= descriptor.fogColorFactorB;
                }
            }

            for (CelestialBodyType body : descriptor.celestialBodies) {
                if (body == CelestialBodyType.BODY_NONE) {
                    // Reset
                    celestialBodies.clear();
                } else {
                    celestialBodies.add(body);
                }
            }


            return this;
        }

        public Builder skyType(SkyType t) {
            this.skyType = t;
            return this;
        }

        public Builder resetSkytype() {
            this.skyType = null;
            return this;
        }

        public Builder sunBrightnessFactor(float f) {
            this.sunBrightnessFactor = f;
            return this;
        }

        public Builder starBrightnessFactor(float f) {
            this.starBrightnessFactor = f;
            return this;
        }

        public Builder skyColorFactor(float r, float g, float b) {
            this.skyColorFactorR = r;
            this.skyColorFactorG = g;
            this.skyColorFactorB = b;
            return this;
        }

        public Builder resetSkyColor() {
            skyColorFactorR = null;
            skyColorFactorG = null;
            skyColorFactorB = null;
            return this;
        }

        public Builder resetFogColor() {
            fogColorFactorR = null;
            fogColorFactorG = null;
            fogColorFactorB = null;
            return this;
        }

        public Builder fogColorFactor(float r, float g, float b) {
            fogColorFactorR = r;
            fogColorFactorG = g;
            fogColorFactorB = b;
            return this;
        }

        public Builder addBody(CelestialBodyType body) {
            celestialBodies.add(body);
            return this;
        }

        public SkyDescriptor build() {
            return new SkyDescriptor(this);
        }
    }
}

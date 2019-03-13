package mcjty.rftools.blocks.security;

import mcjty.lib.thirteen.ConfigSpec;

public class SecurityConfiguration {

    public static final String CATEGORY_SECURITY = "security";
    public static ConfigSpec.BooleanValue enabled;

    public static void init(ConfigSpec.Builder SERVER_BUILDER, ConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the block security system").push(CATEGORY_SECURITY);
        CLIENT_BUILDER.comment("Settings for the block security system").push(CATEGORY_SECURITY);

        enabled = SERVER_BUILDER
                .comment("Whether anything related to the block security system should exist")
                .define("enabled", true);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}

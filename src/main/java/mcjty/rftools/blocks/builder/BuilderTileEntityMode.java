package mcjty.rftools.blocks.builder;

public enum BuilderTileEntityMode {
    MOVE_FORBIDDEN("forbidden"),
    MOVE_WHITELIST("whitelist"),
    MOVE_BLACKLIST("blacklist"),
    MOVE_ALLOWED("allowed");

    private final String name;

    BuilderTileEntityMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static BuilderTileEntityMode find(String name) {
        for (BuilderTileEntityMode config : values()) {
            if (name.equals(config.getName())) {
                return config;
            }
        }
        return MOVE_FORBIDDEN;
    }
}

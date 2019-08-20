package mcjty.rftools.compat.computers;

public class CrafterDriver {
//    public static class OCDriver extends AbstractOCDriver {
//        public OCDriver() {
//            super("rftools_crafter", CrafterBaseTE.class);
//        }
//
//        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<CrafterBaseTE> {
//            public InternalManagedEnvironment(CrafterBaseTE tile) {
//                super(tile, "rftools_crafter");
//            }
//
//            @Callback(doc = "function():number; Get the currently stored energy")
//            public Object[] getEnergy(Context c, Arguments a) {
//                return new Object[]{tile.getStoredPower()};
//            }
//
//            @Callback(doc = "function():number; Get the maximum stored energy")
//            public Object[] getMaxEnergy(Context c, Arguments a) {
//                return new Object[]{tile.getCapacity()};
//            }
//
//            @Callback(doc="function():string; Get the current redstone mode. One of \"Ignored\", \"Off\" and \"On\"")
//            public Object[] getRedstoneMode(Context c, Arguments a) {
//                return new Object[]{tile.getRSMode().getDescription()};
//            }
//
//            @Callback(doc="function(rsMode:string); Set the redstone mode. One of \"Ignored\", \"Off\" and \"On\"")
//            public Object[] setRedstoneMode(Context c, Arguments a) {
//                String newVal = a.checkString(0);
//                RedstoneMode rsMode = RedstoneMode.getMode(newVal);
//
//                if (rsMode != null) {
//                    tile.setRSMode(rsMode);
//                    tile.markDirtyClient();
//                    return new Object[]{true};
//                } else {
//                    return new Object[]{false, "Not a valid redstone mode. Needs to be one of \"Ignored\", \"Off\" and \"On\""};
//                }
//            }
//
//            @Callback(doc = "function():number; Get the speed mode, the returned value will either be 0 (slow), or 1 (fast)")
//            public Object[] getSpeedMode(Context c, Arguments a) {
//                return new Object[]{tile.getSpeedMode()};
//            }
//
//            @Callback(doc = "function(mode:number):bool; Set the speed mode. One of 0 (slow), or 1 (fast)")
//            public Object[] setSpeedMode(Context c, Arguments a) {
//                int mode = a.checkInteger(0);
//                if (mode < 0 || mode > 1) {
//                    return new Object[]{false, "Not a valid speed mode. Needs to be one of 0 (slow), or 1 (fast)"};
//                }
//
//                tile.setSpeedMode(mode);
//                return new Object[]{true};
//            }
//
//            @Callback(doc = "function():number; Get the number of supported recipes")
//            public Object[] getSupportedRecipes(Context c, Arguments a) {
//                return new Object[]{tile.getSupportedRecipes()};
//            }
//
//            @Callback(doc = "function(recipeIndex:number):table; Gets the recipe at the specified index")
//            public Object[] getRecipe(Context c, Arguments a) {
//                /* Lua indexes start at 1 */
//                int index = a.checkInteger(0) - 1;
//                if (index < 0 || index > tile.getSupportedRecipes()) {
//		    return new Object[]{null, "Invalid index"};
//                }
//
//                List<ItemStack> ingredientList = new ArrayList<>();
//                CraftingInventory inv = tile.getRecipe(index).getInventory();
//
//                for (int i = 0; i < inv.getSizeInventory(); i++) {
//                    ingredientList.add(inv.getStackInSlot(i).copy());
//                }
//
//                return new Object[]{ingredientList};
//            }
//
//            @Callback(doc = "function(recipeIndex:number):table; Gets the result of the recipe at the specified index")
//            public Object[] getRecipeResult(Context c, Arguments a) {
//                /* Lua indexes start at 1 */
//                int index = a.checkInteger(0) - 1;
//                if (index < 0 || index > tile.getSupportedRecipes()) {
//                    return new Object[]{null, "Invalid index"};
//                }
//
//                return new Object[]{tile.getRecipe(index).getResult()};
//            }
//
//            @Callback(doc = "function(recipeIndex:number):bool; Returns whether an item is kept in the input slots for the specified recipe")
//            public Object[] getKeepOne(Context c, Arguments a) {
//                /* Lua indexes start at 1 */
//                int index = a.checkInteger(0) - 1;
//                if (index < 0 || index > tile.getSupportedRecipes()) {
//                    return new Object[]{null, "Invalid index"};
//                }
//
//                return new Object[]{tile.getRecipe(index).isKeepOne()};
//            }
//
//            @Callback(doc = "function(recipeIndex:number):string; Gets the craft mode for the specified recipe, one of \"Int\", \"Ext\", or \"ExtC\"")
//            public Object[] getCraftMode(Context c, Arguments a) {
//                /* Lua indexes start at 1 */
//                int index = a.checkInteger(0) - 1;
//                if (index < 0 || index > tile.getSupportedRecipes()) {
//                    return new Object[]{null, "Invalid index"};
//                }
//
//                return new Object[]{tile.getRecipe(index).getCraftMode().getDescription()};
//            }
//
//            @Callback(doc = "function(recipeIndex:number, database:address, slot:number)bool; Store a recipe in a database, starting from the given slot number")
//            public Object[] storeRecipeInDB(Context c, Arguments a) {
//                /* Lua indexes start at 1 */
//                int index = a.checkInteger(0) - 1;
//                if (index < 0 || index > tile.getSupportedRecipes()) {
//                    return new Object[]{false, "Invalid index"};
//                }
//
//                String address = a.checkString(1);
//                Node databaseNode = node().network().node(address);
//                if (databaseNode == null) {
//                    return new Object[]{false, "given component address does not exist"};
//                }
//
//                Environment databaseEnvironment = databaseNode.host();
//                if (databaseEnvironment == null || !(databaseEnvironment instanceof Database)) {
//                    return new Object[]{false, "given component is not a database"};
//                }
//
//                CraftingInventory inv = tile.getRecipe(index).getInventory();
//                Database database = (Database)databaseEnvironment;
//                if (database.size() < inv.getSizeInventory()) {
//                    return new Object[]{false, "Not enough slots in database for recipe"};
//                }
//
//                int slot = a.checkInteger(2) - 1;
//                if (slot < 0 || slot >= database.size()) {
//                    return new Object[]{false, "Slot index out of bounds"};
//                }
//
//                if (database.size() - inv.getSizeInventory() < slot) {
//                    return new Object[]{false, "Not enough slots available from given slot number to end of database"};
//                }
//
//                for (int i = 0; i < inv.getSizeInventory(); i++) {
//                    database.setStackInSlot(slot, inv.getStackInSlot(i).copy());
//                    slot++;
//                }
//
//                return new Object[]{true};
//            }
//
//            @Callback(doc = "function(recipeIndex:number, database:address, slot:number)bool; Store a recipe result in a database, at the given slot number")
//            public Object[] storeRecipeResultInDB(Context c, Arguments a) {
//                /* Lua indexes start at 1 */
//                int index = a.checkInteger(0) - 1;
//                if (index < 0 || index > tile.getSupportedRecipes()) {
//                    return new Object[]{false, "Invalid index"};
//                }
//
//                String address = a.checkString(1);
//                Node databaseNode = node().network().node(address);
//                if (databaseNode == null) {
//                    return new Object[]{false, "given component address does not exist"};
//                }
//
//                Environment databaseEnvironment = databaseNode.host();
//                if (databaseEnvironment == null || !(databaseEnvironment instanceof Database)) {
//                    return new Object[]{false, "given component is not a database"};
//                }
//
//                Database database = (Database)databaseEnvironment;
//                int slot = a.checkInteger(2) - 1;
//                if (slot < 0 || slot >= database.size()) {
//                    return new Object[]{false, "Slot index out of bounds"};
//                }
//
//                database.setStackInSlot(slot, tile.getRecipe(index).getResult().copy());
//
//               return new Object[]{true};
//            }
//
//            @Override
//            public int priority() {
//                return 4;
//            }
//        }
//
//        @Override
//        public AbstractManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side, TileEntity tile) {
//            return new InternalManagedEnvironment((CrafterBaseTE) tile);
//        }
//    }
}

package vapourdrive.furnacemk2.furnace;

import net.minecraft.world.inventory.ContainerData;

public class FurnaceData implements ContainerData {

    public int experience;

    public int cookProgress;
    public int cookMax;
    public int fuel;

    @Override
    public int get(int index) {
//        FurnaceMk2.debugLog(String.format("BP: %s", this.burnProgress));
//        FurnaceMk2.debugLog(String.format("CMB: %s", this.currentMaxBurn));
//        FurnaceMk2.debugLog(String.format("CP: %s", this.cookProgress));
//        FurnaceMk2.debugLog(String.format("EXP: %s", this.experience));
//        FurnaceMk2.debugLog(String.format("CM: %s", this.cookMax));
        return switch (index) {
            case 0 -> this.cookProgress;
            case 1 -> this.cookMax;
            case 2 -> this.experience;
            case 3 -> this.fuel;
            default -> 0;
        };
    }

    @Override
    public void set(int index, int value) {
        switch (index) {
            case 0 -> this.cookProgress = value;
            case 1 -> this.cookMax = value;
            case 2 -> this.experience = value;
            case 3 -> this.fuel = value;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}

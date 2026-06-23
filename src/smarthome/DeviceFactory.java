package smarthome;

public class DeviceFactory {
    private static final String DEFAULT_MAC = "00:00:00:00:00:00";
    private static final double LATEST_FIRMWARE = 2.0;
    private static final double LEGACY_FIRMWARE = 1.0;

    public static SmartDevice createLivingRoomBulb(String id, String name) {
        return new SmartDevice.Builder(id, name)
                .withRoom("Living Room")
                .withMacAddress(DEFAULT_MAC)
                .withFirmwareVersion(LATEST_FIRMWARE)
                .build();
    }

    public static SmartDevice createBasicThermostat(String id) {
        return new SmartDevice.Builder(id, "Thermostat")
                .withFirmwareVersion(LEGACY_FIRMWARE)
                .build();
    }
}
package smarthome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HomeHub {
    private static HomeHub instance;
    private List<ManageableDevice> devices;
    private HomeHub() {
        devices = new ArrayList<>();
    }

    public static HomeHub getInstance() {
        if (instance == null) {
            instance = new HomeHub();
        }
        return instance;
    }

    public void registerDevice(ManageableDevice device) throws DuplicateDeviceException {
        if (device instanceof SmartDevice) {
            SmartDevice newDevice = (SmartDevice) device;
            for (ManageableDevice existing : devices) {
                if (existing instanceof SmartDevice) {
                    SmartDevice existingDevice = (SmartDevice) existing;
                    if (existingDevice.getId().equals(newDevice.getId())) {
                        throw new DuplicateDeviceException("Urządzenie o id " + newDevice.getId() + " już istnieje.");
                    }
                    if (newDevice.getMacAddress() != null
                            && newDevice.getMacAddress().equals(existingDevice.getMacAddress())) {
                        throw new DuplicateDeviceException("Urządzenie o adresie MAC " + newDevice.getMacAddress() + " już istnieje.");
                    }
                }
            }
        }
        devices.add(device);
    }

    public List<ManageableDevice> getAllDevices() {
        return devices;
    }

    public List<SmartDevice> getDevicesByRoom(String room) {
        List<SmartDevice> result = new ArrayList<>();
        for (ManageableDevice device : devices) {
            if (device instanceof SmartDevice) {
                SmartDevice smartDevice = (SmartDevice) device;
                if (Objects.equals(room, smartDevice.getRoom())) {
                    result.add(smartDevice);
                }
            }
        }
        Collections.sort(result);
        return result;
    }
}
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

    public void registerDevice(SmartDevice device) throws DuplicateDeviceException {
        for (ManageableDevice existing : devices) {
            SmartDevice existingDevice = (SmartDevice) existing;
            if (existingDevice.getId().equals(device.getId())) {
                throw new DuplicateDeviceException("Urządzenie o id " + device.getId() + " już istnieje.");
            }
            if (device.getMacAddress() != null
                    && device.getMacAddress().equals(existingDevice.getMacAddress())) {
                throw new DuplicateDeviceException("Urządzenie o adresie MAC " + device.getMacAddress() + " już istnieje.");
            }
        }
        devices.add(device);
    }

    public List<SmartDevice> getDevicesByRoom(String room) {
        List<SmartDevice> result = new ArrayList<>();
        for (ManageableDevice device : devices) {
            SmartDevice smartDevice = (SmartDevice) device;
            if (Objects.equals(room, smartDevice.getRoom())) {
                result.add(smartDevice);
            }
        }
        Collections.sort(result);
        return result;
    }
}
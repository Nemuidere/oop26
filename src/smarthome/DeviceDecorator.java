package smarthome;

public abstract class DeviceDecorator implements ManageableDevice {
    protected final ManageableDevice wrappedDevice;

    public DeviceDecorator(ManageableDevice wrappedDevice) {
        this.wrappedDevice = wrappedDevice;
    }

    @Override
    public void turnOn() {
        wrappedDevice.turnOn();
    }

    @Override
    public void turnOff() {
        wrappedDevice.turnOff();
    }

    @Override
    public String getStatus() {
        return wrappedDevice.getStatus();
    }
}

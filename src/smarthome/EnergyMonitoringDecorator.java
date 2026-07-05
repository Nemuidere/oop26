package smarthome;

import java.time.LocalDateTime;

public class EnergyMonitoringDecorator extends DeviceDecorator {

    public EnergyMonitoringDecorator(ManageableDevice wrappedDevice) {
        super(wrappedDevice);
    }

    @Override
    public void turnOn() {
        super.turnOn();
        System.out.println("Monitorowanie zużycia prądu: urządzenie uruchomione o " + LocalDateTime.now());
    }
}

package smarthome;

import java.util.List;

public class SmartHomeFacade {
    private final HomeHub hub;

    public SmartHomeFacade(HomeHub hub) {
        this.hub = hub;
    }

    public void goodNightRoutine() {
        System.out.println("Uruchamianie rutyny 'Dobranoc'...");
        List<ManageableDevice> devices = hub.getAllDevices();
        for (ManageableDevice device : devices) {
            device.turnOff();
        }
        System.out.println("Obnizanie temperatury na termostatach.");
        System.out.println("Tryb oszczedzania energii wlaczony.");
    }

    public void movieMode() {
        System.out.println("Uruchamianie trybu 'Film'...");
        List<ManageableDevice> devices = hub.getAllDevices();
        for (ManageableDevice device : devices) {
            device.turnOff();
        }
        System.out.println("Wylaczanie glownego oswietlenia.");
        System.out.println("Wlaczanie telewizora.");
    }
}

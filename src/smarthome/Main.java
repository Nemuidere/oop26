package smarthome;

public class Main {
    public static void main(String[] args) {
        try {
            SmartDevice badDevice = new SmartDevice.Builder("d1", "Lampa")
                    .withMacAddress("ZLY-ADRES")
                    .build();
        } catch (InvalidMacAddressException e) {
            System.out.println("Błąd: " + e.getMessage());
        }

        try {
            HomeHub hub = HomeHub.getInstance();
            SmartDevice bulb = DeviceFactory.createLivingRoomBulb("d2", "Lampa salonowa");
            hub.registerDevice(bulb);
            System.out.println("Urzadzenie zarejestrowane poprawnie.");

            LegacyThermostat legacyThermostat = new LegacyThermostat();
            ThermostatAdapter thermostatAdapter = new ThermostatAdapter(legacyThermostat);
            hub.registerDevice(thermostatAdapter);
            System.out.println("Adapter termostatu zarejestrowany poprawnie.");
            thermostatAdapter.turnOn();
            System.out.println(thermostatAdapter.getStatus());

            SmartDevice bulb2 = DeviceFactory.createLivingRoomBulb("d3", "Lampa kuchenna");
            ManageableDevice monitoredBulb = new EnergyMonitoringDecorator(bulb2);
            hub.registerDevice(monitoredBulb);
            System.out.println("Monitorowana zarowka zarejestrowana poprawnie.");
            monitoredBulb.turnOn();
        } catch (DuplicateDeviceException e) {
            System.out.println("Blad: " + e.getMessage());
        }
    }
}
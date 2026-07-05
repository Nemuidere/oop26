package smarthome;

public class LegacyThermostat {
    private boolean heating;
    private double currentTemperature = 21.0;

    public void enableHeating() {
        heating = true;
        System.out.println("LegacyThermostat: ogrzewanie włączone.");
    }

    public void disableHeating() {
        heating = false;
        System.out.println("LegacyThermostat: ogrzewanie wyłączone.");
    }

    public double fetchCurrentTemperature() {
        return currentTemperature;
    }
}

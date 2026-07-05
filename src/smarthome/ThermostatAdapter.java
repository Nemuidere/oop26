package smarthome;

public class ThermostatAdapter implements ManageableDevice {
    private final LegacyThermostat thermostat;

    public ThermostatAdapter(LegacyThermostat thermostat) {
        this.thermostat = thermostat;
    }

    @Override
    public void turnOn() {
        thermostat.enableHeating();
    }

    @Override
    public void turnOff() {
        thermostat.disableHeating();
    }

    @Override
    public String getStatus() {
        return "Termostat - aktualna temperatura: " + thermostat.fetchCurrentTemperature() + " stopni.";
    }
}

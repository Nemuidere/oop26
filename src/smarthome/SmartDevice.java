package smarthome;

public class SmartDevice implements Comparable<SmartDevice> {
    private final String id;
    private final String name;
    private final String room;
    private final String macAddress;
    private final double firmwareVersion;

    private SmartDevice(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.room = builder.room;
        this.macAddress = builder.macAddress;
        this.firmwareVersion = builder.firmwareVersion;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public double getFirmwareVersion() {
        return firmwareVersion;
    }

        @Override
    public int compareTo(SmartDevice other) {
        int byName = this.name.compareTo(other.name);
        if (byName != 0) {
            return byName;
        }
        if (this.room == null && other.room == null) {
            return 0;
        }
        if (this.room == null) {
            return -1;
        }
        if (other.room == null) {
            return 1;
        }
        return this.room.compareTo(other.room);
    }

    public static class Builder {
        private final String id;
        private final String name;
        private String room;
        private String macAddress;
        private double firmwareVersion;

        public Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder withRoom(String room) {
            this.room = room;
            return this;
        }

        public Builder withMacAddress(String macAddress) {
            if (macAddress == null
                    || macAddress.length() != 17
                    || !macAddress.contains(":")) {
                throw new InvalidMacAddressException(
                        "Niewłaściwy format adresu MAC: " + macAddress);
            }
            this.macAddress = macAddress;
            return this;
        }

        public Builder withFirmwareVersion(double firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
            return this;
        }

        public SmartDevice build() {
            return new SmartDevice(this);
        }
    }
}

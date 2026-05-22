// 1. THE INTERFACE (The Contract)
interface NetworkConnectable {
    // Interfaces assume methods are public and abstract by default
    void connectToWiFi(String networkName);
    void disconnect();
}

// 2. THE ABSTRACT CLASS (The Partially Finished Blueprint)
abstract class Device {
    private String name;
    private String brand;
    protected boolean isPoweredOn; // 'protected' means subclasses can see it directly

    public Device(String name, String brand) {
        this.name = name;
        this.brand = brand;
        this.isPoweredOn = false;
    }

    public String getName() { return name; }
    public String getBrand() { return brand; }
    
    public void turnOn() { this.isPoweredOn = true; }

    // ABSTRACT METHOD: No body {}. Every subclass MUST provide its own implementation.
    public abstract void printDeviceSpecs(); 
}

// 3. THE CONCRETE SUBCLASS (Fills in the blanks AND signs the contract)
class Laptop extends Device implements NetworkConnectable {
    private int ramSizeGB;

    public Laptop(String name, String brand, int ramSizeGB) {
        super(name, brand);
        this.ramSizeGB = ramSizeGB;
    }

    // Fulfilling the Abstract Class requirement
    @Override
    public void printDeviceSpecs() {
        System.out.println("Laptop: " + getBrand() + " " + getName() + " with " + ramSizeGB + "GB RAM.");
    }

    // Fulfilling the Interface requirement
    @Override
    public void connectToWiFi(String networkName) {
        if (isPoweredOn) {
            System.out.println(getName() + " is now connected to " + networkName);
        } else {
            System.out.println("Cannot connect to Wi-Fi. " + getName() + " is off.");
        }
    }

    @Override
    public void disconnect() {
        System.out.println(getName() + " disconnected from network.");
    }
}

// 4. MAIN EXECUTION
public class task6 {
    public static void main(String[] args) {
        // Device dev = new Device("Generic", "Brand"); // ERROR: Cannot instantiate abstract class!

        Laptop myMac = new Laptop("MacBook Pro", "Apple", 16);
        
        myMac.printDeviceSpecs(); // Calls the required abstract method implementation
        
        myMac.connectToWiFi("Home_Network_5G"); // Fails, device is off
        myMac.turnOn(); // Shared logic from the abstract base class
        myMac.connectToWiFi("Home_Network_5G"); // Succeeds!
    }
}

/*
1. Abstract Classes (The "IS-A" Relationship)
Making a class abstract does two things:

Prevents direct instantiation: new Device() will now throw a compiler error. You can only instantiate its subclasses.

Allows abstract methods: You can define a method signature without a body, forcing every subclass to write its own custom version.

Think of an abstract class as a partially finished blueprint. It provides shared variables and logic (like turnOn()), but leaves some blanks for the subclasses to fill in.

2. Interfaces (The "CAN-DO" Relationship)
While a class can only extend one parent class, it can implement multiple interfaces. An interface is a pure contract. It doesn't hold data (state); it only defines actions that a class must be capable of performing.

If Device is what the object is, an Interface is what the object can do.

The Code: Abstract Classes & Interfaces in Action
Let's refactor our system. We will make Device abstract, and we'll introduce a NetworkConnectable interface because not all devices (like a basic calculator) can connect to Wi-Fi.
 */
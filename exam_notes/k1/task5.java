import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 1. THE BASE CLASS
class Device {
    private String name;
    private String brand;
    private boolean isPoweredOn;

    public Device(String name, String brand) {
        this.name = name;
        this.brand = brand;
        this.isPoweredOn = false; // Default state
    }

    public String getName() { return name; }
    public String getBrand() { return brand; }
    public boolean getIsPoweredOn() { return isPoweredOn; }

    public void turnOn() { this.isPoweredOn = true; }
    public void turnOff() { this.isPoweredOn = false; }

    public void printStatus() {
        System.out.println(brand + " " + name + " is " + (isPoweredOn ? "ON" : "OFF"));
    }
}

// 2. THE SUBCLASS
class Laptop extends Device {
    private int batteryLevel;

    public Laptop(String name, String brand, int batteryLevel) {
        super(name, brand); // Calls the Device constructor
        this.batteryLevel = batteryLevel;
    }

    public int getBatteryLevel() { return batteryLevel; }
    public void charge(int amount) { this.batteryLevel += amount; }

    @Override
    public void printStatus() {
        super.printStatus(); // Runs the Device version first
        System.out.println(" -> Battery: " + batteryLevel + "%");
    }
}

// 3. THE MANAGER CLASS (Where the magic happens)
class Inventory {
    // List: Keeps the order you add items, allows duplicates.
    private List<Device> allDevices;
    
    // Set: Does NOT allow duplicates. Great for unique lists.
    private Set<String> uniqueBrands;

    public Inventory() {
        this.allDevices = new ArrayList<>();
        this.uniqueBrands = new HashSet<>();
    }

    public void addDevice(Device device) {
        allDevices.add(device);
        uniqueBrands.add(device.getBrand()); // If brand exists, the Set just ignores it
    }

    // --- LOGIC, LOOPS, AND CONDITIONS ---

    public void powerOnAllLaptops() {
        System.out.println("\n--- Powering on Laptops ---");
        // ENHANCED FOR LOOP: Best when you just need to look at every item once
        for (Device device : allDevices) {
            
            // IF & INSTANCEOF: Check if this specific Device is actually a Laptop
            if (device instanceof Laptop) {
                device.turnOn(); // Mutator from the base class
                System.out.println("Turned on: " + device.getName());
            }
        }
    }

    public void printLowBatteryWarning() {
        System.out.println("\n--- Battery Check ---");
        // TRADITIONAL FOR LOOP: Best when you need the index number
        for (int i = 0; i < allDevices.size(); i++) {
            Device current = allDevices.get(i);

            if (current instanceof Laptop) {
                // CASTING: We know it's a Laptop now, but Java still sees it as a "Device"
                // We have to cast it to Laptop to access getBatteryLevel()
                Laptop laptop = (Laptop) current; 
                
                // NESTED IF: Checking object state
                if (laptop.getBatteryLevel() < 20) {
                    System.out.println("Warning! " + laptop.getName() + " is low (" + laptop.getBatteryLevel() + "%). Location index: " + i);
                }
            }
        }
    }

    public void drainBatteries() {
        System.out.println("\n--- Draining Batteries ---");
        int index = 0;
        
        // WHILE LOOP: Runs as long as the condition is true
        while (index < allDevices.size()) {
            Device dev = allDevices.get(index);
            
            if (dev instanceof Laptop && dev.getIsPoweredOn()) {
                Laptop lap = (Laptop) dev;
                lap.charge(-10); // Modifying object state
                System.out.println(lap.getName() + " battery dropped to " + lap.getBatteryLevel() + "%");
            }
            index++; // Don't forget this, or you get an infinite loop!
        }
    }

    public void showUniqueBrands() {
        System.out.println("\n--- Unique Brands ---");
        for (String brand : uniqueBrands) {
            System.out.println("- " + brand);
        }
    }
}

// 4. MAIN EXECUTION
public class task5 {
    public static void main(String[] args) {
        Inventory myInventory = new Inventory();

        // Instantiate our objects
        Laptop macBook = new Laptop("MacBook Pro", "Apple", 15);
        Laptop thinkPad = new Laptop("ThinkPad T14", "Lenovo", 85);
        Device desktop = new Device("OptiPlex 7090", "Dell");
        Laptop air = new Laptop("MacBook Air", "Apple", 50); // Duplicate brand

        // Add them to the system
        myInventory.addDevice(macBook);
        myInventory.addDevice(thinkPad);
        myInventory.addDevice(desktop);
        myInventory.addDevice(air);

        // Run the logic
        myInventory.showUniqueBrands();
        myInventory.powerOnAllLaptops();
        myInventory.printLowBatteryWarning();
        myInventory.drainBatteries();
    }
}

/*
Deep Dive: How the Pieces Connect
Lists vs. Sets
In OOP, you rarely have standalone objects. You manage them in collections.

List: We use an ArrayList here because we want to keep track of every single device in the exact order it was added. If we have five identical MacBooks, the List will hold all five.

Set: We use a HashSet to track the brands. Sets guarantee uniqueness. Notice in main we added two "Apple" laptops. When we loop through the Set in showUniqueBrands(), "Apple" only prints once. The Set silently rejects duplicates without throwing an error.

The Enhanced for Loop + instanceof
Look at powerOnAllLaptops(). The enhanced for loop reads like English: "For every Device in allDevices..."

Because allDevices is a list of Device, Java treats everything inside it as a generic Device. It doesn't know if the item is a Laptop or a desktop PC. By using if (device instanceof Laptop), we ask Java to check the object's true DNA at runtime. If it's a Laptop, we turn it on.

The Traditional for Loop + Type Casting
Look at printLowBatteryWarning(). We use for (int i = 0; i < allDevices.size(); i++) because we actually want to print the index i to know where the device is located.

Here is the most critical OOP concept in the method:

Java
Laptop laptop = (Laptop) current;
Even after checking if (current instanceof Laptop), the variable current is still technically of type Device. A base Device doesn't have a getBatteryLevel() method. To use the Laptop-specific methods, we must cast it. This tells the compiler: "Trust me, I checked. Treat this Device specifically as a Laptop."

The while Loop + Compound Logic
In drainBatteries(), we use a while loop. While loops are great when your progression through a collection might not be perfectly linear (though here we just increment index++).

Notice the compound if statement: if (dev instanceof Laptop && dev.getIsPoweredOn()). We are chaining logic: we only care about the object if it is a Laptop AND its internal boolean state is true. We then use the mutator charge(-10) to alter the object's data.
 */
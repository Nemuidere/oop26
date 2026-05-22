import java.util.ArrayList;
import java.util.List;

// 1. THE ABSTRACT BASE
abstract class Device {
    private String name;

    public Device(String name) { this.name = name; }
    public String getName() { return name; }

    // Every device boots up, but they all do it differently
    public abstract void bootUp();
}

// 2. SUBCLASS A
class Laptop extends Device {
    public Laptop(String name) { super(name); }

    @Override
    public void bootUp() {
        System.out.println(getName() + " blinks its keyboard backlight and shows a login screen.");
    }

    // --- COMPILE-TIME POLYMORPHISM (OVERLOADING) ---
    // Same method name, different parameters. Java knows which one to use at compile time.
    public void charge() {
        System.out.println(getName() + " is charging at standard speed (5W).");
    }

    public void charge(int wattage) {
        System.out.println(getName() + " is fast-charging at " + wattage + "W!");
    }
}

// 3. SUBCLASS B
class SmartTV extends Device {
    public SmartTV(String name) { super(name); }

    @Override
    public void bootUp() {
        System.out.println(getName() + " flashes a giant brand logo and boots into Netflix.");
    }
}

// 4. THE EXECUTION
public class task7 {
    public static void main(String[] args) {
        // We create a list of the generic type "Device"
        List<Device> techStack = new ArrayList<>();
        
        // Polymorphic assignment: A Laptop IS-A Device, a SmartTV IS-A Device
        techStack.add(new Laptop("MacBook Pro"));
        techStack.add(new SmartTV("Sony Bravia 4K"));
        techStack.add(new Laptop("ThinkPad"));

        System.out.println("--- RUNTIME POLYMORPHISM ---");
        
        // One loop, one exact same method call (.bootUp()), three different outcomes
        for (Device currentDevice : techStack) {
            // Java looks at the ACTUAL object type in memory at runtime,
            // not the reference type (Device), to decide which code to run.
            currentDevice.bootUp(); 
        }

        System.out.println("\n--- COMPILE-TIME POLYMORPHISM ---");
        
        // We need to target a Laptop specifically to show overloading
        Laptop myMac = (Laptop) techStack.get(0);
        
        myMac.charge();         // Triggers version 1 (no arguments)
        myMac.charge(96);       // Triggers version 2 (takes an int)
    }
}

/*
1. The Power of Runtime Polymorphism
Look at the for loop in main:

Java
for (Device currentDevice : techStack) {
    currentDevice.bootUp();
}
This is the crown jewel of OOP. The loop doesn't know—and doesn't care—whether currentDevice is a Laptop or a SmartTV. It just knows it's a Device, and all Devices have a bootUp() method.

If you add a SmartFridge, a Smartphone, and a PlayStation class tomorrow, this loop code never has to change. You just override bootUp() in those new classes, drop them into the list, and Java handles the rest. This keeps your code incredibly flexible and scalable.

2. How Compile-Time Polymorphism (Overloading) Differs
Overloading happens inside a single class (like Laptop). It has nothing to do with parent/child relationships.

Java
public void charge() { ... }
public void charge(int wattage) { ... }
You are simply giving developers different ways to call the same conceptual action. Java looks at the arguments you pass inside the parentheses. If you pass nothing, it links to the first one. If you pass an integer, it links to the second. This is locked in before your code even runs.
*/
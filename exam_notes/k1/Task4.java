import java.util.ArrayList;
import java.util.List;

// --- KROK 6: Własny Wyjątek ---
// W Pythonie: class FullStationException(Exception): pass
class FullStationException extends Exception {
    public FullStationException(String message) {
        super(message); // Przekazujemy wiadomość do bazowej klasy Exception
    }
}

// --- KROK 2: Klasa Abstrakcyjna ---
// "abstract" oznacza, że nie możesz zrobić: new Vehicle(1). 
// To tylko szablon dla innych pojazdów.
abstract class Vehicle {
    private int id; // private = tylko ta klasa widzi ID (Enkapsulacja)

    public Vehicle(int id) {
        this.id = id;
    }

    // Akcesor (Getter) - w Pythonie użyłbyś @property
    public int getId() {
        return id;
    }
}

// --- KROK 1 & 2: Klasy Podrzędne ---
class Bicycle extends Vehicle {
    public Bicycle(int id) {
        super(id); // super(id) wywołuje konstruktor klasy Vehicle
    }
}

class Scooter extends Vehicle {
    private int batteryLevel; // Zakres 0-100

    public Scooter(int id, int batteryLevel) {
        super(id);
        // Prosta walidacja (dobra praktyka w Javie)
        if (batteryLevel < 0) this.batteryLevel = 0;
        else if (batteryLevel > 100) this.batteryLevel = 100;
        else this.batteryLevel = batteryLevel;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }
}

// --- KROK 3: Stacja ---
class Station {
    private String id;
    private int capacity;
    // Używamy ArrayList zamiast tablicy, bo jest elastyczna jak lista w Pythonie
    // <Vehicle> oznacza, że lista przechowuje Rowery i Hulajnogi naraz (Polimorfizm!)
    private List<Vehicle> vehicles;

    public Station(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.vehicles = new ArrayList<>();
    }

    // KROK 5 & 6: Zwracanie pojazdu
    public void returnVehicle(Vehicle v) throws FullStationException {
        if (vehicles.size() >= capacity) {
            // W Pythonie: raise FullStationException("...")
            throw new FullStationException("Brak wolnych uchwytów na stacji: " + id);
        }
        vehicles.add(v);
    }

    // KROK 4: Wypożyczenie po ID
    public Vehicle rentVehicle(int id) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getId() == id) {
                // remove(i) zwraca usuwany obiekt (wygodne!)
                return vehicles.remove(i);
            }
        }
        return null; // Odpowiednik None w Pythonie
    }

    // KROK 7: Metody specjalistyczne
    public Bicycle rentBicycle() {
        for (int i = 0; i < vehicles.size(); i++) {
            // "instanceof" sprawdza, czy obiekt jest danej klasy (Python: isinstance())
            if (vehicles.get(i) instanceof Bicycle) {
                return (Bicycle) vehicles.remove(i); // Rzutowanie (casting) na rower
            }
        }
        return null;
    }

    public Scooter rentScooter() {
        Scooter bestScooter = null;
        int bestIdx = -1;

        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i) instanceof Scooter) {
                Scooter current = (Scooter) vehicles.get(i);
                // Szukamy najwyższego naładowania
                if (bestScooter == null || current.getBatteryLevel() > bestScooter.getBatteryLevel()) {
                    bestScooter = current;
                    bestIdx = i;
                }
            }
        }

        if (bestIdx != -1) {
            return (Scooter) vehicles.remove(bestIdx);
        }
        return null;
    }
}

// --- KROK 8: Testy i Main ---
public class Task4 {
    public static void main(String[] args) {
        // Przygotowanie stacji do testów
        Station stacja = new Station("Centrum", 10);

        try {
            // Test 1: Brak hulajnóg
            System.out.println("Test 1 (brak): " + stacja.rentScooter()); // Powinno być null

            // Test 2: Dokładnie jedna hulajnoga
            stacja.returnVehicle(new Scooter(101, 50));
            Scooter s1 = stacja.rentScooter();
            System.out.println("Test 2 (jedna): ID=" + s1.getId() + ", Bateria=" + s1.getBatteryLevel());

            // Test 3: Więcej niż jedna (wybór najlepszej)
            stacja.returnVehicle(new Scooter(201, 20));
            stacja.returnVehicle(new Scooter(202, 95));
            stacja.returnVehicle(new Scooter(203, 60));
            
            Scooter best = stacja.rentScooter();
            System.out.println("Test 3 (najlepsza): ID=" + best.getId() + " (oczekiwane 202)");

        } catch (FullStationException e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }
}
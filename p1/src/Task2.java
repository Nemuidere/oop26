import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;

// Interfejs definiujący kontrakt dla obiektów, które potrafią wygenerować swoją reprezentację w formacie SVG.
// Stosujemy go, aby wymusić obecność metody toSvg() w różnych, niepowiązanych klasach.
interface SvgRepresentative {
    String toSvg();
}

// Klasa abstrakcyjna Clock definiuje wspólne zachowanie dla wszystkich typów zegarów.
// Przechowujemy tu czas (godziny, minuty, sekundy).
abstract class Clock {
    protected int hour;
    protected int minute;
    protected int second;

    // Metoda ustawia czas systemowy. Używamy LocalTime dla wygody i precyzji.
    public void setCurrentTime() {
        LocalTime now = LocalTime.now();
        setTime(now.getHour(), now.getMinute(), now.getSecond());
    }

    // Kluczowa metoda z walidacją. Rzucanie wyjątków (IllegalArgumentException) to standardowa metoda
    // sygnalizowania błędnych danych wejściowych w Javie.
    public void setTime(int hour, int minute, int second) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
            throw new IllegalArgumentException("Niepoprawny format czasu: " + hour + ":" + minute + ":" + second);
        }
        this.hour = hour;
        this.minute = minute;
        this.second = second;

        // Po każdej zmianie czasu musimy poinformować wskazówki (jeśli istnieją), by zaktualizowały swoje kąty.
        updateHands();
    }

    // Metoda "haczyk" (hook), którą nadpiszemy w klasie potomnej, aby obsłużyć wskazówki.
    protected abstract void updateHands();
}

// AnalogClock to konkretna implementacja zegara.
// Zawiera listę wskazówek, co pozwala na wykorzystanie POLIMORFIZMU – traktujemy różne wskazówki tak samo.
class AnalogClock extends Clock implements SvgRepresentative {
    // Lista polimorficzna – może zawierać HourHand, MinuteHand i SecondHand.
    private final List<ClockHand> hands = new ArrayList<>();

    public AnalogClock() {
        // Inicjalizacja wskazówek – każda ma inną logikę, ale ten sam typ bazowy.
        hands.add(new HourHand());
        hands.add(new MinuteHand());
        hands.add(new SecondHand());
    }

    @Override
    protected void updateHands() {
        // Wykorzystujemy polimorfizm: wywołujemy setTime na każdym obiekcie z listy,
        // a Java sama wie, którą wersję metody (godzinną, minutową czy sekundową) uruchomić.
        for (ClockHand hand : hands) {
            hand.setTime(hour, minute, second);
        }
    }

    @Override
    public String toSvg() {
        // Budujemy napis SVG. Pamiętaj o kolejności: najpierw tarcza (pod spodem), potem wskazówki (na wierzchu).
        StringBuilder svg = new StringBuilder();
        svg.append("<svg width='200' height='200' viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'>\n");
        // Tarcza zegara (koło)
        svg.append("  <circle cx='100' cy='100' r='95' stroke='black' stroke-width='2' fill='white' />\n");

        // Dodajemy kod SVG każdej wskazówki
        for (ClockHand hand : hands) {
            svg.append("  ").append(hand.toSvg()).append("\n");
        }

        svg.append("</svg>");
        return svg.toString();
    }

    @Override
    public String toString() {
        return "Zegar_" + String.format("%02d-%02d-%02d", hour, minute, second);
    }
}

// Klasa bazowa dla wskazówek. Używamy pola 'angle' do przechowywania stanu obrotu.
abstract class ClockHand implements SvgRepresentative {
    protected double angle;

    // Każda wskazówka musi zdefiniować, jak przelicza czas na kąt.
    public abstract void setTime(int h, int m, int s);

    // Wspólna metoda generująca linię SVG obróconą o zadany kąt wokół środka (100, 100).
    protected String drawHand(int length, int width, String color) {
        // rotate(kąt, środekX, środekY) to najprostszy sposób na obrót w SVG.
        return String.format("<line x1='100' y1='100' x2='100' y2='%d' stroke='%s' stroke-width='%d' transform='rotate(%.2f, 100, 100)' />",
                100 - length, color, width, angle);
    }
}

class SecondHand extends ClockHand {
    @Override
    public void setTime(int h, int m, int s) {
        // 360 stopni / 60 sekund = 6 stopni na sekundę.
        this.angle = s * 6.0;
    }

    @Override
    public String toSvg() {
        return drawHand(80, 1, "red"); // Sekundnik: długi, cienki, czerwony.
    }
}

class MinuteHand extends ClockHand {
    @Override
    public void setTime(int h, int m, int s) {
        // Ruch ciągły: minuty + (sekundy / 60). 360/60 = 6 stopni na minutę.
        this.angle = (m + s / 60.0) * 6.0;
    }

    @Override
    public String toSvg() {
        return drawHand(70, 3, "black"); // Minutowa: średnia, grubsza.
    }
}

class HourHand extends ClockHand {
    @Override
    public void setTime(int h, int m, int s) {
        // Ruch ciągły: (h % 12) + (m / 60) + (s / 3600). 360/12 = 30 stopni na godzinę.
        this.angle = ((h % 12) + m / 60.0 + s / 3600.0) * 30.0;
    }

    @Override
    public String toSvg() {
        return drawHand(50, 5, "black"); // Godzinna: krótka, najgrubsza.
    }
}

// Klasa reprezentująca dane z pliku CSV.
class City {
    private final String name;
    private final int timezone;

    public City(String name, int timezone) {
        this.name = name;
        this.timezone = timezone;
    }

    // Metoda wczytująca listę miast. Bardzo ważne: obsługa wyjątków I/O i parsowanie linii.
    public static List<City> fromCsv(String path) {
        List<City> cities = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // Pomijamy nagłówek CSV
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String name = parts[0].trim();
                    int tz = Integer.parseInt(parts[1].trim());
                    cities.add(new City(name, tz));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Błąd podczas odczytu miast: " + e.getMessage());
        }
        return cities;
    }

    // Generowanie plików i katalogów. Używamy biblioteki java.nio.file, która jest nowoczesna i wygodna.
    public static void generateAnalogClocksSvg(List<City> cities, AnalogClock clock) {
        // Tworzymy nazwę katalogu na podstawie toString() zegara (np. Zegar_12-00-00)
        Path dirPath = Paths.get(clock.toString());
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }

            for (City city : cities) {
                // Dla każdego miasta ustawiamy czas zegara (oryginalna godzina + przesunięcie strefy)
                // Uwaga: To uproszczenie, w rzeczywistości należałoby obsłużyć przejście przez północ (mod 24).
                int cityHour = (clock.hour + city.timezone) % 24;
                if (cityHour < 0) cityHour += 24;

                clock.setTime(cityHour, clock.minute, clock.second);

                // Zapisujemy do pliku .svg
                Path filePath = dirPath.resolve(city.name + ".svg");
                Files.writeString(filePath, clock.toSvg());
            }
            System.out.println("Wygenerowano pliki w katalogu: " + dirPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Klasa startowa do uruchomienia i przetestowania logiki.
public class Task2 {
    public static void main(String[] args) {
        // 1. Tworzymy zegar i ustawiamy czas (np. 10:15:30)
        AnalogClock clock = new AnalogClock();
        try {
            clock.setTime(10, 15, 30);

            // 2. Wczytujemy miasta (upewnij się, że strefy.csv jest w głównym folderze projektu)
            List<City> cities = City.fromCsv("strefy.csv");

            // 3. Generujemy pliki SVG dla wszystkich miast
            if (!cities.isEmpty()) {
                City.generateAnalogClocksSvg(cities, clock);
            } else {
                System.out.println("Brak danych o miastach (sprawdź plik strefy.csv)");
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }
}
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * KLASA GŁÓWNA - Tutaj możesz przetestować działanie programu.
 * Nazwij plik: Solution.java
 */
public class Task1 {
    public static void main(String[] args) {
        System.out.println("--- Start programu testowego ---");

        // Tutaj możesz dodać testy, np.:
        // DeathCauseStatisticList list = new DeathCauseStatisticList();
        // list.repopulate("zgony.csv");
        // list.mostDeadlyDiseases(25, 3).forEach(s -> System.out.println(s.getIcd10Code()));
    }
}

// ==========================================
// ZADANIE 1 i 2: Model statystyki zgonu
// ==========================================

class DeathCauseStatistic {
    // Zadanie 1: Prywatne pola
    private String icd10Code;
    private int[] deathCounts; // Tablica liczb zgonów w kolejnych grupach wiekowych

    // Konstruktor do tworzenia obiektów
    public DeathCauseStatistic(String icd10Code, int[] deathCounts) {
        this.icd10Code = icd10Code;
        this.deathCounts = deathCounts;
    }

    // Zadanie 1: Akcesor (Getter) dla kodu ICD-10
    public String getIcd10Code() {
        return icd10Code;
    }

    /**
     * Zadanie 1: Metoda statyczna tworząca obiekt z linii CSV.
     * CSV format: KOD, RAZEM, 0-4, 5-9, 10-14 ...
     */
    public static DeathCauseStatistic fromCsvLine(String line) {
        // split(",") dzieli linię na tablicę Stringów wg przecinka
        String[] parts = line.split(",");

        // Wykładowca wspomniał o tabulatorze po kodzie. trim() usunie tabulator i spacje.
        String code = parts[0].trim();

        // Zgodnie z poleceniem: liczby zgonów są od 3. kolumny (index 2) wzwyż.
        // Index 0 to kod, Index 1 to suma całkowita (którą pomijamy w tablicy grup).
        int[] counts = new int[parts.length - 2];
        for (int i = 2; i < parts.length; i++) {
            String val = parts[i].trim();
            // Jeśli w pliku jest "-", traktujemy to jako 0 zgonów.
            if (val.equals("-")) {
                counts[i - 2] = 0;
            } else {
                try {
                    counts[i - 2] = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    counts[i - 2] = 0; // Zabezpieczenie przed błędnymi danymi
                }
            }
        }
        return new DeathCauseStatistic(code, counts);
    }

    // ==========================================
    // ZADANIE 2: Przedziały wiekowe
    // ==========================================

    /**
     * Zadanie 2: Publiczna klasa wewnętrzna trzymająca dane o przedziale
     */
    public static class AgeBracketDeaths {
        public final int young;      // Dolna granica wieku
        public final int old;        // Górna granica wieku
        public final int deathCount; // Liczba zgonów

        public AgeBracketDeaths(int young, int old, int deathCount) {
            this.young = young;
            this.old = old;
            this.deathCount = deathCount;
        }
    }

    /**
     * Zadanie 2: Metoda znajdująca przedział dla konkretnego wieku.
     * Grupy w CSV są co 5 lat: 0-4, 5-9, 10-14...
     */
    public AgeBracketDeaths getAgeBracketDeaths(int age) {
        // Obliczamy indeks: wiek 7 -> 7/5 = 1 (druga grupa, czyli index 1)
        int index = age / 5;

        // Jeśli wiek jest bardzo wysoki, przypisujemy go do ostatniej grupy (95+)
        if (index >= deathCounts.length) {
            index = deathCounts.length - 1;
        }

        int youngBoundary = index * 5;
        int oldBoundary = youngBoundary + 4;
        // W ostatniej grupie "95 i więcej" granica górna mogłaby być inna,
        // ale trzymamy się logiki z zadania.

        return new AgeBracketDeaths(youngBoundary, oldBoundary, deathCounts[index]);
    }
}

// ==========================================
// ZADANIE 3: Lista statystyk i sortowanie
// ==========================================

class DeathCauseStatisticList {
    // Lista przechowująca wszystkie obiekty statystyk
    private List<DeathCauseStatistic> statistics = new ArrayList<>();

    /**
     * Zadanie 3: Metoda czyszcząca i ładująca dane z pliku CSV
     */
    public void repopulate(String path) {
        statistics.clear(); // Usunięcie istniejących danych
        try {
            // Files.readAllLines wczytuje cały plik do pamięci (dobre dla średnich plików)
            List<String> lines = Files.readAllLines(Paths.get(path));

            // i=1, bo pomijamy nagłówek (pierwszą linię pliku)
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                // Pomijamy puste linie lub wiersz sumaryczny "OGÓŁEM"
                if (line.trim().isEmpty() || line.contains("OGÓŁEM")) continue;

                statistics.add(DeathCauseStatistic.fromCsvLine(line));
            }
        } catch (IOException e) {
            System.err.println("Problem z plikiem CSV: " + e.getMessage());
        }
    }

    /**
     * Zadanie 3: Wyznaczanie n najbardziej zabójczych chorób dla danego wieku
     */
    public List<DeathCauseStatistic> mostDeadlyDiseases(int age, int n) {
        // Stream API to najwygodniejszy sposób na sortowanie i filtrowanie kolekcji
        return statistics.stream()
                .sorted((s1, s2) -> {
                    // Porównujemy liczbę zgonów w przedziale wiekowym do którego należy 'age'
                    int d1 = s1.getAgeBracketDeaths(age).deathCount;
                    int d2 = s2.getAgeBracketDeaths(age).deathCount;
                    // d2 vs d1 (kolejność odwrotna) daje sortowanie MALEJĄCE
                    return Integer.compare(d2, d1);
                })
                .limit(n) // Wybieramy tylko n pierwszych elementów
                .collect(Collectors.toList()); // Zbieramy z powrotem do listy
    }
}

// ==========================================
// ZADANIE 4: Interfejs ICD i optymalizacje
// ==========================================

/**
 * Zadanie 4: Interfejs definujący pobieranie opisu kodu ICD-10
 */
interface ICDCodeTabular {
    String getDescription(String code) throws IndexOutOfBoundsException;
}

/**
 * Optymalizacja pod CZAS: Szybkie wyszukiwanie dzięki HashMap (kosztem RAMu)
 */
class ICDCodeTabularOptimizedForTime implements ICDCodeTabular {
    private Map<String, String> cache = new HashMap<>();

    public ICDCodeTabularOptimizedForTime(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            // Dane właściwe są od linii 88 (index 87)
            for (int i = 87; i < lines.size(); i++) {
                String line = lines.get(i).trim();

                // Walidacja kodu: Litera + 2 cyfry (np. A01)
                if (isValidICD10(line)) {
                    // split(" ", 2) dzieli na kod i resztę opisu (maksymalnie 2 części)
                    String[] parts = line.split(" ", 2);
                    if (parts.length == 2) {
                        cache.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd: " + e.getMessage());
        }
    }

    // Pomocnicza metoda sprawdzająca format kodu: Litera + Cyfra + Cyfra
    private boolean isValidICD10(String line) {
        if (line.length() < 3) return false;
        return Character.isLetter(line.charAt(0)) &&
                Character.isDigit(line.charAt(1)) &&
                Character.isDigit(line.charAt(2));
    }

    @Override
    public String getDescription(String code) {
        if (!cache.containsKey(code)) {
            throw new IndexOutOfBoundsException("Nie znaleziono opisu dla kodu: " + code);
        }
        return cache.get(code);
    }
}

/**
 * Optymalizacja pod PAMIĘĆ: Nie przechowujemy danych, przeszukujemy plik za każdym razem
 */
class ICDCodeTabularOptimizedForMemory implements ICDCodeTabular {
    private String path;

    public ICDCodeTabularOptimizedForMemory(String path) {
        this.path = path;
    }

    @Override
    public String getDescription(String code) {
        // BufferedReader czyta plik linia po linii, nie ładując całego pliku do RAMu
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            int currentLine = 0;
            while ((line = reader.readLine()) != null) {
                currentLine++;
                // Skip pierwszych 87 linii
                if (currentLine < 88) continue;

                String trimmed = line.trim();
                // Sprawdzamy czy linia zaczyna się od szukanego kodu
                if (trimmed.startsWith(code)) {
                    String[] parts = trimmed.split(" ", 2);
                    if (parts.length == 2) return parts[1].trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd odczytu: " + e.getMessage());
        }
        throw new IndexOutOfBoundsException("Brak opisu dla: " + code);
    }
}
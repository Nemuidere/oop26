import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

// =============================================================================
// KONCEPCJA: WYJĄTKI WŁASNE
// W Javie tworzymy własne wyjątki, aby precyzyjnie obsługiwać błędy biznesowe.
// Dziedziczenie po Exception wymusza obsługę błędu (checked exception).
// =============================================================================

class AmbigiousProductException extends Exception {
    private final List<String> products;

    public AmbigiousProductException(List<String> products) {
        super("Znaleziono więcej niż jeden produkt o podanym prefiksie.");
        this.products = products;
    }

    public List<String> getProducts() {
        return products;
    }
}

// =============================================================================
// KONCEPCJA: ABSTRAKCJA I POLIMORFIZM
// Klasa Product jest abstrakcyjna, bo "sam produkt" nie istnieje - musi to być
// albo jedzenie (Food), albo coś innego (NonFood).
// Słowo 'abstract' zabrania tworzenia obiektu: new Product() - i o to chodzi!
// =============================================================================

abstract class Product {
    protected String name; // protected pozwala klasom pochodnym na dostęp do pola
    protected Double[] prices;

    public Product(String name, Double[] prices) {
        this.name = name;
        this.prices = prices;
    }

    public String getName() {
        return name;
    }

    /**
     * Oblicza indeks w tablicy na podstawie daty.
     * Skoro dane zaczynają się od stycznia 2010 (indeks 0),
     * to luty 2010 to indeks 1, itd.
     */
    protected int getIndex(int year, int month) {
        return (year - 2010) * 12 + (month - 1);
    }

    // Metoda abstrakcyjna: każda klasa (jedzenie/nie-jedzenie) musi po swojemu
    // zaimplementować pobieranie ceny, bo formaty plików CSV są inne.
    public abstract double getPrice(int year, int month);

    // =============================================================================
    // STATYCZNA METODA FABRYCZNA (KROK 4)
    // To jest serce zadania. Metoda szuka pliku w katalogach i decyduje,
    // czy stworzyć obiekt FoodProduct czy NonFoodProduct.
    // =============================================================================
    public static Product fromPrefix(String prefix) throws AmbigiousProductException {
        List<Product> foundProducts = new ArrayList<>();

        // Szukamy w obu folderach: food i nonfood
        searchInDir("food", prefix, foundProducts, true);
        searchInDir("nonfood", prefix, foundProducts, false);

        if (foundProducts.isEmpty()) {
            throw new NoSuchElementException("Nie znaleziono produktu o prefiksie: " + prefix);
        }
        if (foundProducts.size() > 1) {
            // Jeśli znaleźliśmy kilka, rzucamy nasz własny wyjątek z listą nazw
            List<String> names = foundProducts.stream().map(Product::getName).collect(Collectors.toList());
            throw new AmbigiousProductException(names);
        }

        return foundProducts.get(0);
    }

    private static void searchInDir(String dir, String prefix, List<Product> result, boolean isFood) {
        File folder = new File(dir);
        if (!folder.exists()) return;

        File[] files = folder.listFiles((d, name) -> name.endsWith(".csv"));
        if (files == null) return;

        for (File file : files) {
            try {
                // Czytamy tylko pierwszą linię, żeby sprawdzić nazwę produktu
                String productName = Files.lines(file.toPath()).findFirst().orElse("");
                if (productName.startsWith(prefix)) {
                    if (isFood) {
                        result.add(FoodProduct.fromCsv(file.toPath()));
                    } else {
                        result.add(NonFoodProduct.fromCsv(file.toPath()));
                    }
                }
            } catch (IOException e) {
                // Ignorujemy błędy odczytu pojedynczych plików
            }
        }
    }
}

// =============================================================================
// KONCEPCJA: DZIEDZICZENIE (NON-FOOD)
// Prostsza wersja: jedna linia cen dla całego kraju.
// =============================================================================

class NonFoodProduct extends Product {
    public NonFoodProduct(String name, Double[] prices) {
        super(name, prices);
    }

    public static NonFoodProduct fromCsv(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            String name = lines.get(0);
            // Ceny są w 3. linii (indeks 2), rozdzielone średnikami
            Double[] prices = Arrays.stream(lines.get(2).split(";"))
                    .map(v -> Double.valueOf(v.replace(",", ".")))
                    .toArray(Double[]::new);
            return new NonFoodProduct(name, prices);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getPrice(int year, int month) {
        return prices[getIndex(year, month)];
    }
}

// =============================================================================
// KONCEPCJA: DZIEDZICZENIE (FOOD)
// Trudniejsza wersja: ceny zależą od województwa.
// =============================================================================

class FoodProduct extends Product {
    // Mapa przechowująca ceny dla każdego województwa osobno
    private final Map<String, Double[]> regionalPrices = new HashMap<>();

    public FoodProduct(String name) {
        super(name, null);
    }

    public void addRegionPrice(String region, Double[] prices) {
        regionalPrices.put(region, prices);
    }

    public static FoodProduct fromCsv(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            FoodProduct fp = new FoodProduct(lines.get(0));
            // Ceny zaczynają się od 3. linii. Każda linia to inne województwo.
            for (int i = 2; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(";");
                String region = parts[0];
                Double[] prices = Arrays.stream(parts)
                        .skip(1) // Pomijamy nazwę województwa
                        .map(v -> Double.valueOf(v.replace(",", ".")))
                        .toArray(Double[]::new);
                fp.addRegionPrice(region, prices);
            }
            return fp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getPrice(int year, int month) {
        // Na potrzeby kolokwium przyjmujemy np. województwo MAZOWIECKIE
        // lub średnią. Tutaj pobierzemy pierwsze dostępne z brzegu.
        Double[] p = regionalPrices.get("POLSKA");
        if (p == null) p = regionalPrices.values().iterator().next();
        return p[getIndex(year, month)];
    }
}

// =============================================================================
// KONCEPCJA: AGREGACJA (KOSZYK)
// Klasa Cart "posiada" produkty. To jest relacja typu "has-a".
// Koszyk nie wie, czy produkt to szampon czy kiełbasa - używa polimorfizmu.
// =============================================================================

class Cart {
    // Mapujemy produkt na jego ilość w koszyku
    private final Map<Product, Integer> items = new HashMap<>();

    public void addProduct(Product product, int amount) {
        // merge to sprytna metoda: jeśli produkt jest, dodaj do ilości, jeśli nie - wstaw
        items.merge(product, amount, Integer::sum);
    }

    public double getPrice(int year, int month) {
        double total = 0;
        for (var entry : items.entrySet()) {
            // Tutaj dzieje się MAGIA POLIMORFIZMU:
            // Wywołujemy getPrice(), a Java sama wie, czy użyć wersji z Food czy NonFood!
            total += entry.getKey().getPrice(year, month) * entry.getValue();
        }
        return total;
    }

    public double getInflation(int y1, int m1, int y2, int m2) {
        double price1 = getPrice(y1, m1);
        double price2 = getPrice(y2, m2);

        // Obliczamy różnicę miesięcy
        int months = (y2 - y1) * 12 + (m2 - m1);

        // Wzór z zadania: ((p2-p1)/p1) * 100 / months * 12
        return ((price2 - price1) / price1) * 100 / months * 12;
    }
}

// =============================================================================
// MAIN: TESTOWANIE WSZYSTKIEGO
// =============================================================================

public class Task3 {
    public static void main(String[] args) {
        Cart cart = new Cart();

        try {
            // Przykład 1: Sukces
            Product p1 = Product.fromPrefix("Cebula");
            cart.addProduct(p1, 2);

            // Przykład 2: Ambigious (więcej niż jeden)
            // Jeśli mamy pliki "Jabłka..." i "Jaja...", wpisanie "Ja" rzuci błąd.
            Product p2 = Product.fromPrefix("Ja");

        } catch (AmbigiousProductException e) {
            System.out.println("BŁĄD: " + e.getMessage());
            System.out.println("Dostępne opcje: " + e.getProducts());
        } catch (NoSuchElementException e) {
            System.out.println("BŁĄD: Nie znaleziono takiego produktu.");
        }

        // Wyświetlenie wartości koszyka dla przykładowej daty
        System.out.println("Wartość koszyka (03.2022): " + cart.getPrice(2022, 3));
    }
}
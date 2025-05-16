import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class CurrencyConverter {

    private static final String API_URL = "https://api.privatbank.ua/p24api/pubinfo?exchange&coursid=5";
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        double amount = getAmountFromUser();
        String currency = getCurrencyFromUser();

        try {
            String json = getJsonFromPrivatBank();
            double rate = getSaleRateForCurrency(json, currency);

            if (rate == -1) {
                System.out.println("Ошибка: валюта не найдена.");
            } else {
                double result = amount * rate;
                System.out.printf("%.2f %s = %.2f UAH (по курсу продажи %.2f)%n", amount, currency, result, rate);
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Ошибка при получении данных: " + e.getMessage());
        }
    }

    private double getAmountFromUser() {
        while (true) {
            System.out.print("Введите сумму: ");
            String input = scanner.nextLine().trim();
            try {
                if (input.contains(" ")) {
                    throw new NumberFormatException("Ожидалась только одна сумма");
                }
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число без пробелов.");
            }
        }
    }

    private String getCurrencyFromUser() {
        while (true) {
            System.out.print("Введите валюту (USD, EUR, PLN): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.contains(" ")) {
                System.out.println("Ошибка: введите только одну валюту без пробелов.");
                continue;
            }

            if (input.equals("USD") || input.equals("EUR") || input.equals("PLN")) {
                return input;
            } else {
                System.out.println("Ошибка: валюта не поддерживается.");
            }
        }
    }

    private String getJsonFromPrivatBank() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private double getSaleRateForCurrency(String json, String currencyCode) {
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.getString("ccy").equalsIgnoreCase(currencyCode)) {
                return Double.parseDouble(obj.getString("sale"));
            }
        }
        return -1;
    }
}

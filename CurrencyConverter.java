import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
public class CurrencyConverter
{
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/";
    public static void main(String[]args)
    {
        Scanner scanner=new Scanner(System.in);
        System.out.println("Welcome to the Interactive Currency Converter!");
        String baseCurrency=selectCurrency(scanner, "base");
        Map<String, Double> exchangeRates=fetchExchangeRates(baseCurrency);
        if(exchangeRates==null)
        {
            System.out.println("Failed to fetch exchange rates. Please try again later.");
            return;
        }
        String targetCurrency=selectCurrency(scanner, "target", exchangeRates.keySet());
        double amount=getAmount(scanner);
        double convertedAmount=convertCurrency(amount, baseCurrency, targetCurrency, exchangeRates);
        System.out.printf("%.2f %s is equal to %.2f %s%n", amount, baseCurrency, convertedAmount, targetCurrency);
        scanner.close();
    }
    private static String selectCurrency(Scanner scanner, String type)
    {
        System.out.print("Enter the base currency code (e.g., USD, EUR): ");
        return scanner.nextLine().toUpperCase();
    }
    private static Map<String, Double> fetchExchangeRates(String baseCurrency) {
        try
        {
            URL url=new URL(API_URL + baseCurrency);
            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if(conn.getResponseCode() != 200) 
            {
                System.out.println("Error fetching exchange rates: " + conn.getResponseCode());
                return null;
            }
            BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response=new StringBuilder();
            String inputLine;
            while((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
            in.close();
            return parseExchangeRates(response.toString());
        }
        catch (Exception e)
        {
            System.out.println("Exception occurred: " + e.getMessage());
            return null;
        }
    }
    private static Map<String, Double> parseExchangeRates(String jsonResponse) {
        Map<String, Double> rates = new HashMap<>();
        String[] parts = jsonResponse.split("\"rates\":\\{")[1].split("\\}")[0].split(",");
        for(String part : parts)
        {
            String[] rate = part.split(":");
            rates.put(rate[0].replace("\"", ""), Double.parseDouble(rate[1]));
        }
        return rates;
    }
    private static String selectCurrency(Scanner scanner, String type, Iterable<String> availableCurrencies)
    {
        System.out.println("Available currencies:");
        for (String currency : availableCurrencies)
        {
            System.out.println(currency);
        }
        System.out.print("Enter the target currency code: ");
        return scanner.nextLine().toUpperCase();
    }
    private static double getAmount(Scanner scanner)
    {
        double amount=0;
        while (true)
        {
            System.out.print("Enter the amount to convert: ");
            try{
                amount = Double.parseDouble(scanner.nextLine());
                if(amount > 0)
                {
                    break;
                }
                else
                {
                    System.out.println("Amount must be positive. Please try again.");
                }
            }
            catch(NumberFormatException e)
            {
                System.out.println("Please enter a valid number.");
            }
        }
        return amount;
    }
    private static double convertCurrency(double amount, String baseCurrency, String targetCurrency, Map<String, Double> exchangeRates)
    {
        double baseRate=exchangeRates.get(baseCurrency);
        double targetRate=exchangeRates.get(targetCurrency);
        return amount*(targetRate/baseRate);
    }
}
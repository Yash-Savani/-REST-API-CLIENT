package WeatherApp;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class WeatherAppGUI extends JFrame {

    private JTextField cityField;
    private JTextArea outputArea;
    private final String apiKey = "70474bc969c025c98c7658e59c874dab"; 

    public WeatherAppGUI() {
        setTitle("Weather App");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input panel
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Enter City:"));
        cityField = new JTextField(20);
        JButton fetchButton = new JButton("Get Weather");
        topPanel.add(cityField);
        topPanel.add(fetchButton);
        add(topPanel, BorderLayout.NORTH);

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Button action
        fetchButton.addActionListener(e -> fetchWeather());
    }

    private void fetchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            showMessage("Please enter a city.");
            return;
        }

        try {
            String urlString =
                    "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                    "&appid=" + apiKey + "&units=metric";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                showMessage("City not found or API error.");
                return;
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            conn.disconnect();

            JSONObject obj = new JSONObject(response.toString());

            String cityName = obj.getString("name");
            String countryCode = obj.getJSONObject("sys").getString("country");
            String flagEmoji = countryToEmoji(countryCode);

            JSONObject main = obj.getJSONObject("main");
            double temp = main.getDouble("temp");
            double feels = main.getDouble("feels_like");
            int humidity = main.getInt("humidity");

            String weather = obj.getJSONArray("weather").getJSONObject(0).getString("description");

            long timestamp = obj.getLong("dt"); 
            String timeString = Instant.ofEpochSecond(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy h:mm a"));

            // Output
            outputArea.setText("");
            outputArea.append("📍 " + cityName + " " + flagEmoji + "\n");
            outputArea.append("🕒 Time: " + timeString + "\n\n");
            outputArea.append("🌡️ Temperature: " + temp + " °C\n");
            outputArea.append("🤒 Feels Like: " + feels + " °C\n");
            outputArea.append("💧 Humidity: " + humidity + " %\n");
            outputArea.append("🌥️ Condition: " + capitalizeWords(weather) + "\n");

        } catch (Exception e) {
            showMessage("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String countryToEmoji(String countryCode) {
        int OFFSET = 0x1F1E6;
        StringBuilder emoji = new StringBuilder();
        countryCode.toUpperCase().chars().forEach(c -> emoji.appendCodePoint(OFFSET + (c - 'A')));
        return emoji.toString();
    }

    private String capitalizeWords(String text) {
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0)))
              .append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WeatherAppGUI().setVisible(true));
    }
}

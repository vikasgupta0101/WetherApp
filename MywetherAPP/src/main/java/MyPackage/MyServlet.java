package MyPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public MyServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // API Setup
        String apiKey = "b39898950a9217b97d92b8c14cd094d8";
        
        // Get the city from the form input and trim it
        String city = request.getParameter("city").trim(); 
        
        // Validate city input
        if (city == null || city.isEmpty()) {
            request.setAttribute("error", "City name cannot be empty.");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }
        
        // URL Encode the city name to handle spaces or special characters
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        
        // Create the URL for the OpenWeatherMap API request
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + encodedCity + "&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // Successful API request
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                
                Scanner scanner = new Scanner(reader);
                StringBuilder responseContent = new StringBuilder();

                while (scanner.hasNext()) {
                    responseContent.append(scanner.nextLine());
                }
                scanner.close();
                
                // Parse the JSON response
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);
                
                // Date & Time
                long dateTimestamp = jsonObject.get("dt").getAsLong() * 1000;
                String date = new Date(dateTimestamp).toString();
                
                // Temperature
                double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
                int temperatureCelsius = (int) (temperatureKelvin - 273.15);
               
                // Humidity
                int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
                
                // Wind Speed
                double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
                
                // Weather Condition
                String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
                
                // Set the data as request attributes for the JSP page
                request.setAttribute("date", date);
                request.setAttribute("city", city);
                request.setAttribute("temperature", temperatureCelsius);
                request.setAttribute("weatherCondition", weatherCondition);
                request.setAttribute("humidity", humidity);
                request.setAttribute("windSpeed", windSpeed);
                request.setAttribute("weatherData", responseContent.toString());
                
            } else {
                // Handle error response from the API
                request.setAttribute("error", "Failed to retrieve weather data. City not found or invalid request.");
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            request.setAttribute("error", "An error occurred while fetching weather data.");
        }

        // Forward the request to the JSP page for rendering
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}

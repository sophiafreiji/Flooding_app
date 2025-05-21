package com.example.flood

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flood.adapter.SensorDataAdapter
import com.example.flood.api.PredictionRequest
import com.example.flood.api.PredictionResponse
import com.example.flood.api.RetrofitClient
import com.example.flood.api.SensorData
import com.example.flood.api.SensorDataResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    // Value display TextViews
    private lateinit var temperatureValue: TextView
    private lateinit var humidityValue: TextView
    private lateinit var waterLevelValue: TextView
    private lateinit var rainfallValue: TextView
    private lateinit var soilMoistureValue: TextView

    // Hidden EditText fields
    private lateinit var temperatureInput: EditText
    private lateinit var humidityInput: EditText
    private lateinit var waterLevelInput: EditText
    private lateinit var rainfallInput: EditText
    private lateinit var soilMoistureInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize hidden views
        temperatureInput = findViewById(R.id.temperatureInput)
        humidityInput = findViewById(R.id.humidityInput)
        waterLevelInput = findViewById(R.id.waterLevelInput)
        rainfallInput = findViewById(R.id.rainfallInput)
        soilMoistureInput = findViewById(R.id.soilMoistureInput)

        // Initialize display views
        temperatureValue = findViewById(R.id.temperatureValue)
        humidityValue = findViewById(R.id.humidityValue)
        waterLevelValue = findViewById(R.id.waterLevelValue)
        rainfallValue = findViewById(R.id.rainfallValue)
        soilMoistureValue = findViewById(R.id.soilMoistureValue)

        val resultText = findViewById<TextView>(R.id.resultText)
        val predictButton = findViewById<Button>(R.id.btnPredict)
        val refreshButton = findViewById<Button>(R.id.btnRefresh)
        progressBar = findViewById(R.id.progressBar)

        // Configure RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.dataRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load data on startup
        loadSensorData()

        // Prediction button

        predictButton.setOnClickListener {
            // Validate inputs (we still use the hidden EditText for logic)
            if (validateInputs(temperatureInput, humidityInput, waterLevelInput, rainfallInput, soilMoistureInput)) {
                showProgress(true)

                // Convert inputs
                val temperature = temperatureInput.text.toString().toFloatOrNull() ?: 0.0f
                val humidity = humidityInput.text.toString().toFloatOrNull() ?: 0.0f
                val soilMoisture = soilMoistureInput.text.toString().toFloatOrNull() ?: 0.0f
                val waterLevel = waterLevelInput.text.toString().toFloatOrNull() ?: 0.0f
                val rainfall = rainfallInput.text.toString().toFloatOrNull() ?: 0.0f

                // Log values for debugging
                Log.d("MainActivity", "Sending prediction request with: " +
                        "temperature=$temperature, humidity=$humidity, soil_moisture=$soilMoisture, " +
                        "water_level=$waterLevel, rainfall=$rainfall")

                val request = PredictionRequest(
                    temperature = temperature,
                    humidity = humidity,
                    soil_moisture = soilMoisture,
                    water_level = waterLevel,
                    rainfall = rainfall
                )

                RetrofitClient.api.predictFlood(request).enqueue(object : Callback<PredictionResponse> {
                    override fun onResponse(
                        call: Call<PredictionResponse>,
                        response: Response<PredictionResponse>
                    ) {
                        showProgress(false)
                        if (response.isSuccessful && response.body() != null) {
                            val prediction = response.body()?.prediction
                            val message = if (prediction == 1) {
                                "Flood risk detected!"
                            } else {
                                "No flood risk detected"
                            }
                            resultText.text = "Result: $message"
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            Log.e("MainActivity", "Error response: $errorBody")
                            resultText.text = "Error: ${response.code()} - ${response.message()}"
                            Toast.makeText(this@MainActivity,
                                "Server error: $errorBody",
                                Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                        showProgress(false)
                        Log.e("MainActivity", "Network failure", t)
                        resultText.text = "Connection error: ${t.javaClass.simpleName}"
                        Toast.makeText(this@MainActivity,
                            "Connection error: ${t.message}",
                            Toast.LENGTH_LONG).show()
                        t.printStackTrace()
                    }
                })
            }
        }

        // Refresh data button
        refreshButton.setOnClickListener {
            loadSensorData()
        }
    }

    private fun loadSensorData() {
        showProgress(true)
        RetrofitClient.api.getSensorData().enqueue(object : Callback<SensorDataResponse> {
            override fun onResponse(
                call: Call<SensorDataResponse>,
                response: Response<SensorDataResponse>
            ) {
                showProgress(false)
                if (response.isSuccessful && response.body() != null) {
                    val sensorData = response.body()?.data
                    if (sensorData != null && sensorData.isNotEmpty()) {
                        // Update RecyclerView with data
                        updateSensorDataUI(sensorData)
                    } else {
                        Toast.makeText(this@MainActivity,
                            "No sensor data available",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity,
                        "Data loading error: ${response.code()}",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SensorDataResponse>, t: Throwable) {
                showProgress(false)
                Toast.makeText(this@MainActivity,
                    "Connection error: ${t.message}",
                    Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }

    private fun updateSensorDataUI(sensorData: List<SensorData>) {
        // Update RecyclerView with data
        val recyclerView = findViewById<RecyclerView>(R.id.dataRecyclerView)
        val adapter = SensorDataAdapter(sensorData)
        recyclerView.adapter = adapter

        // Pre-fill fields with the latest data
        if (sensorData.isNotEmpty()) {
            val latestData = sensorData[0] // First is the most recent as sorted on server

            // Update hidden EditText to maintain existing logic
            temperatureInput.setText(latestData.temperature.toString())
            humidityInput.setText(latestData.humidity.toString())
            waterLevelInput.setText(latestData.water_level.toString())
            rainfallInput.setText(latestData.rainfall.toString())
            soilMoistureInput.setText(latestData.soil_moisture.toString())

            // Update visible TextViews for display with proper units - all using % now
            temperatureValue.text = "${latestData.temperature} %"
            humidityValue.text = "${latestData.humidity} %"
            waterLevelValue.text = "${latestData.water_level} %"
            rainfallValue.text = "${latestData.rainfall} %"
            soilMoistureValue.text = "${latestData.soil_moisture} %"
        }
    }

    private fun validateInputs(vararg editTexts: EditText): Boolean {
        for (editText in editTexts) {
            if (editText.text.toString().trim().isEmpty()) {
                // Show Toast instead of error since EditText are hidden
                Toast.makeText(this, "Incomplete sensor data", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
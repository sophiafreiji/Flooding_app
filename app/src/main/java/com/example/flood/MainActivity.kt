package com.example.flood

import android.os.Bundle
import android.util.Log  // Ajout de l'import manquant pour Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Suppression de l'import qui cause l'erreur
import com.example.flood.api.PredictionRequest  // Ajout de l'import manquant pour PredictionRequest
import com.example.flood.api.PredictionResponse
import com.example.flood.api.RetrofitClient
import com.example.flood.api.SensorData
import com.example.flood.api.SensorDataResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialiser les vues
        val temperatureInput = findViewById<EditText>(R.id.temperatureInput)
        val humidityInput = findViewById<EditText>(R.id.humidityInput)
        val windSpeedInput = findViewById<EditText>(R.id.windSpeedInput)
        val waterLevelInput = findViewById<EditText>(R.id.waterLevelInput)
        val rainInput = findViewById<EditText>(R.id.rainInput)
        val resultText = findViewById<TextView>(R.id.resultText)
        val predictButton = findViewById<Button>(R.id.btnPredict)
        val refreshButton = findViewById<Button>(R.id.btnRefresh)
        progressBar = findViewById(R.id.progressBar)

        // Configurer RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.dataRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Charger les données au démarrage
        loadSensorData()

        // Bouton de prédiction
        predictButton.setOnClickListener {
            // Vérifier les entrées
            if (validateInputs(temperatureInput, humidityInput, windSpeedInput, waterLevelInput, rainInput)) {
                showProgress(true)

                // Convertir les entrées
                val temperature = temperatureInput.text.toString().toFloatOrNull() ?: 0.0f
                val humidity = humidityInput.text.toString().toFloatOrNull() ?: 0.0f
                val windSpeed = windSpeedInput.text.toString().toFloatOrNull() ?: 0.0f
                val waterLevel = waterLevelInput.text.toString().toFloatOrNull() ?: 0.0f
                val rain = rainInput.text.toString().toIntOrNull() ?: 0

                // Log les valeurs pour débogage
                Log.d("MainActivity", "Sending prediction request with: " +
                        "temperature=$temperature, humidity=$humidity, wind_speed=$windSpeed, " +
                        "water_level=$waterLevel, rain=$rain")

                val request = PredictionRequest(
                    temperature = temperature,
                    humidity = humidity,
                    wind_speed = windSpeed,
                    water_level = waterLevel,
                    rain = rain
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
                                "Risque d'inondation détecté!"
                            } else {
                                "Aucun risque d'inondation détecté"
                            }
                            resultText.text = "Résultat : $message"
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Erreur inconnue"
                            Log.e("MainActivity", "Error response: $errorBody")
                            resultText.text = "Erreur : ${response.code()} - ${response.message()}"
                            Toast.makeText(this@MainActivity,
                                "Erreur serveur: $errorBody",
                                Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                        showProgress(false)
                        Log.e("MainActivity", "Network failure", t)
                        resultText.text = "Erreur de connexion: ${t.javaClass.simpleName}"
                        Toast.makeText(this@MainActivity,
                            "Erreur de connexion: ${t.message}",
                            Toast.LENGTH_LONG).show()
                        t.printStackTrace()
                    }
                })
            }
        }

        // Bouton de rafraîchissement des données
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
                        // Mettre à jour le RecyclerView avec les données
                        updateSensorDataUI(sensorData)
                    } else {
                        Toast.makeText(this@MainActivity,
                            "Aucune donnée de capteur disponible",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity,
                        "Erreur de chargement des données: ${response.code()}",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SensorDataResponse>, t: Throwable) {
                showProgress(false)
                Toast.makeText(this@MainActivity,
                    "Erreur de connexion: ${t.message}",
                    Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }

    private fun updateSensorDataUI(sensorData: List<SensorData>) {
        // Mettre à jour le RecyclerView avec les données
        val recyclerView = findViewById<RecyclerView>(R.id.dataRecyclerView)
        val adapter = com.example.flood.adapter.SensorDataAdapter(sensorData)
        recyclerView.adapter = adapter

        // Vous pouvez aussi pré-remplir les champs avec la dernière donnée
        if (sensorData.isNotEmpty()) {
            val latestData = sensorData[0] // La première est la plus récente car triée côté serveur
            findViewById<EditText>(R.id.temperatureInput).setText(latestData.temperature.toString())
            findViewById<EditText>(R.id.humidityInput).setText(latestData.humidity.toString())
            findViewById<EditText>(R.id.windSpeedInput).setText(latestData.wind_speed.toString())
            findViewById<EditText>(R.id.waterLevelInput).setText(latestData.water_level.toString())
            findViewById<EditText>(R.id.rainInput).setText(latestData.rain.toString())
        }
    }

    private fun validateInputs(vararg editTexts: EditText): Boolean {
        for (editText in editTexts) {
            if (editText.text.toString().trim().isEmpty()) {
                editText.error = "Ce champ est requis"
                return false
            }
        }
        return true
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
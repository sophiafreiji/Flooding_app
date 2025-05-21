package com.example.flood.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flood.R
import com.example.flood.api.SensorData
import java.text.SimpleDateFormat
import java.util.Locale

class SensorDataAdapter(private val sensorDataList: List<SensorData>) :
    RecyclerView.Adapter<SensorDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timestamp: TextView = view.findViewById(R.id.textTimestamp)
        val temperature: TextView = view.findViewById(R.id.textTemperature)
        val humidity: TextView = view.findViewById(R.id.textHumidity)
        val windSpeed: TextView = view.findViewById(R.id.textWindSpeed)
        val waterLevel: TextView = view.findViewById(R.id.textWaterLevel)
        val rain: TextView = view.findViewById(R.id.textRain)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sensorData = sensorDataList[position]

        // Format de date pour l'affichage
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        try {
            // Supposons que timestamp est une chaîne ISO 8601
            val parsedDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .parse(sensorData.timestamp)
            holder.timestamp.text = if (parsedDate != null) dateFormat.format(parsedDate) else sensorData.timestamp
        } catch (e: Exception) {
            // En cas d'erreur de format, afficher la chaîne brute
            holder.timestamp.text = sensorData.timestamp
        }

        holder.temperature.text = String.format("%.1f°C", sensorData.temperature)
        holder.humidity.text = String.format("%.1f%%", sensorData.humidity)
        holder.windSpeed.text = String.format("%.1f m/s", sensorData.wind_speed)
        holder.waterLevel.text = String.format("%.2f m", sensorData.water_level)
        holder.rain.text = if (sensorData.rain == 1) "Oui" else "Non"
    }

    override fun getItemCount() = sensorDataList.size
}
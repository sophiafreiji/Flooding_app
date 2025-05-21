package com.example.flood.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flood.R
import com.example.flood.api.SensorData

class SensorDataAdapter(private val sensorDataList: List<SensorData>) :
    RecyclerView.Adapter<SensorDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTimestamp: TextView = view.findViewById(R.id.textTimestamp)
        val textTemperature: TextView = view.findViewById(R.id.textTemperature)
        val textHumidity: TextView = view.findViewById(R.id.textHumidity)
        val textSoilMoisture: TextView = view.findViewById(R.id.textSoilMoisture)
        val textWaterLevel: TextView = view.findViewById(R.id.textWaterLevel)
        val textRainfall: TextView = view.findViewById(R.id.textRainfall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = sensorDataList[position]

        holder.textTimestamp.text = data.timestamp
        holder.textTemperature.text = "${data.temperature}%"
        holder.textHumidity.text = "${data.humidity}%"
        holder.textSoilMoisture.text = "${data.soil_moisture}%"
        holder.textWaterLevel.text = "${data.water_level}%"
        holder.textRainfall.text = "${data.rainfall}%"
    }

    override fun getItemCount() = sensorDataList.size
}
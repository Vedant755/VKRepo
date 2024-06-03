package com.ftg.carrepo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ftg.carrepo.R

class DummyDetailsAdapter(
    private val details: List<String>,
    private val isAdmin: Boolean
) :RecyclerView.Adapter<DummyDetailsAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val key : TextView = itemView.findViewById(R.id.key)
        val checkBox : CheckBox = itemView.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vehicle_details_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return details.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key: String = details[position]

        if(!isAdmin)
            holder.checkBox.visibility = View.GONE

        if (key == "VEHICLE DETAILS:-" || key == "AGENCY:-" || key.isBlank()) {
            holder.key.text = key
            holder.key.textSize = 12f
        } else {
            holder.key.text = key
        }
    }


}
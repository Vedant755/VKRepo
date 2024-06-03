package com.ftg.carrepo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ftg.carrepo.Models.SearchedVehicleDetails
import com.ftg.carrepo.R

class SearchAdapter(
    private var vehicles: List<SearchedVehicleDetails>,
    private val searchByRc: Boolean,
    private val isTwoColumn: Boolean,
    private val select: (SearchedVehicleDetails) -> Unit
): RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val NumberTV: TextView = itemView.findViewById(R.id.itemTxt)
        val ModelTV: TextView = itemView.findViewById(R.id.modelName)
        val row: LinearLayout = itemView.findViewById(R.id.main_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.searched_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return vehicles.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vehicle = if(isTwoColumn){
            if(position % 2 == 0){
                vehicles[position/2]
            }else{
                vehicles[(vehicles.size/2) + (position/2)]
            }
        }else{
            vehicles[position]
        }

        holder.NumberTV.text = if(searchByRc) {
            val rcNo = vehicle?.rc_no

            if (rcNo != null && rcNo.length == 8 && rcNo.matches(Regex("[A-Za-z]{2}[0-9]{6}"))) {
                StringBuilder(rcNo).apply {
                    insert(2, '-')
                }.apply {
                    insert(5, '-')
                }.toString()
                // Use formattedRCNo as needed
            } else if(vehicle?.rc_no!!.length == 11){
                StringBuilder(vehicle.rc_no).apply {
                    insert(2, '-')
                }.apply {
                    insert(5, '-')
                }.apply {
                    insert(9, '-')
                }.toString()
            }else if(vehicle?.rc_no!!.length==9){
                StringBuilder(vehicle.rc_no).apply {
                    insert(2, '-')
                }.apply {
                    insert(5, '-')
                }.apply {
                    insert(7, '-')
                }.toString()
            }else if(vehicle?.rc_no!!.length==10){
                StringBuilder(vehicle.rc_no).apply {
                    insert(2, '-')
                }.apply {
                    insert(5, '-')
                }.apply {
                    insert(8, '-')
                }.toString()
            }
            else{
                StringBuilder(vehicle.rc_no).apply {
                    insert(2, '-')
                }.apply {
                    insert(5, '-')
                }.apply {
                    insert(8, '-')
                }.toString()
            }
        }else{
            vehicle.chassis_no
        }

        if(isTwoColumn){
            holder.ModelTV.visibility = View.GONE
            val params = holder.NumberTV.layoutParams as LinearLayout.LayoutParams
            params.weight = 0.9f
            holder.NumberTV.layoutParams = params
        }else{
            holder.ModelTV.visibility = View.VISIBLE
            holder.ModelTV.text = vehicle?.mek_and_model
            val params = holder.NumberTV.layoutParams as LinearLayout.LayoutParams
            params.weight = 0.5f
            holder.NumberTV.layoutParams = params
        }

        holder.row.setOnClickListener {
            select(vehicle)
        }
    }

    fun updateData(newData: List<SearchedVehicleDetails>) {
        vehicles = newData
        notifyDataSetChanged()
    }
}
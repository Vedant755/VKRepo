package com.ftg.carrepo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ftg.carrepo.Models.Branch
import com.ftg.carrepo.Models.LoadBranchResponse
import com.ftg.carrepo.Models.VehicleDetails
import com.ftg.carrepo.R
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ItemAdapter(
    private var mList:List<Branch>,
    private var vList: List<VehicleDetails>,
    private var listener: ItemChangeListener
):RecyclerView.Adapter<ItemAdapter.ViewHolder>(){
    interface ItemChangeListener {
        fun onBranchChange(item: Branch)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.item.text = item.name

        val matchingVehicle = vList.find { it.branch_id == item._id }
        if (matchingVehicle != null) {
            val parsedUpdatedAt: ZonedDateTime = ZonedDateTime.parse(matchingVehicle.updatedAt)
            val zUpdatedAt: ZonedDateTime = parsedUpdatedAt.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
            val fmtUpdatedAt = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH)
            holder.date.text = fmtUpdatedAt.format(zUpdatedAt)
        } else {
            val parsedCreatedAt: ZonedDateTime = ZonedDateTime.parse(item.createdAt)
            val zCreatedAt: ZonedDateTime = parsedCreatedAt.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
            val fmtCreatedAt = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH)
            holder.date.text = fmtCreatedAt.format(zCreatedAt)
        }

        val headOfficeNames = item.head_offices.joinToString { it.name }
        holder.sub.text = headOfficeNames
        holder.row.setOnClickListener {
            listener.onBranchChange(item)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }


    class ViewHolder(ItemView: View):RecyclerView.ViewHolder(ItemView){
        val item: TextView = ItemView.findViewById(R.id.tvSelect)
        val date : TextView = ItemView.findViewById(R.id.date)
        val sub: TextView = ItemView.findViewById(R.id.headOfficeName)
        val row: LinearLayout = ItemView.findViewById(R.id.main)
    }

}

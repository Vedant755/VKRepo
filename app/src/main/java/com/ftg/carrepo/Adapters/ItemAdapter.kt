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
import com.ftg.carrepo.R
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ItemAdapter(
    private var mList:List<Branch>,
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
        holder.item.text=item.name
        val parsed: ZonedDateTime = ZonedDateTime.parse(item.createdAt)
        val z: ZonedDateTime = parsed.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
        val fmt = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH)
        holder.date.text=fmt.format(z)
        val headOfficeNames = item.head_offices.joinToString { it.name }
        holder.sub.text = headOfficeNames
        holder.row.setOnClickListener{
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

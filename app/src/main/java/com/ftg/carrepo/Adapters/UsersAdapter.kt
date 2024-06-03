package com.ftg.carrepo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.R

class UsersAdapter(
    private val users: List<UserDetails>,
    private val select: (UserDetails) -> Unit
): RecyclerView.Adapter<UsersAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.user_name)
        val mobile: TextView = itemView.findViewById(R.id.user_mobile)
        val card: CardView = itemView.findViewById(R.id.user_card)
        val admin: ImageView = itemView.findViewById(R.id.user_admin)
        val active: ImageView = itemView.findViewById(R.id.user_active)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]

        if(user.role=="ADMIN")
            holder.admin.visibility = View.VISIBLE

//        if(user.status==2)
//            holder.active.setImageResource(R.drawable.cross)

        holder.name.text = user.name
        user.mobile?.let { holder.mobile.text = it.toString() }
        holder.card.setOnClickListener { select(user) }
    }
}
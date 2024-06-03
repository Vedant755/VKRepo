package com.ftg.carrepo.Adapters

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.net.Uri
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.ftg.carrepo.R

class DetailsAdapter(
    private val details: List<Map<String, String>>,
    private val isAdmin: Boolean,
    private val context: Context,
    private var showCheckBox: Boolean,
    private val contactListener: ContactListener
) :RecyclerView.Adapter<DetailsAdapter.ViewHolder>(){
    private var onClickListener:OnClickListener? =null
    var sb = StringBuffer()
    interface ContactListener {
        fun onContactClicked(number: String)
    }
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val key : TextView = itemView.findViewById(R.id.key)
        val value : TextView = itemView.findViewById(R.id.value)
        val divider : TextView = itemView.findViewById(R.id.divider)
        val checkBox : CheckBox = itemView.findViewById(R.id.checkBox)
        val layout: RelativeLayout = itemView.findViewById(R.id.itemm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vehicle_details_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return details.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key: String = details[position].keys.toTypedArray()[0]
        val value: String = details[position][key].toString()
        if(!isAdmin)
            holder.checkBox.visibility = View.GONE
        if (key=="Level 1con" || key == "Level 2con" || key == "Level 3con" || key == "Level 4con" || key == "Contact 1" || key == "Contact 2" || key == "Contact 3"){
            holder.value.autoLinkMask = Linkify.PHONE_NUMBERS
        }else{
            holder.value.autoLinkMask = 0
        }
        if (!showCheckBox)
            holder.checkBox.visibility = View.GONE
        if (key == "VEHICLE DETAILS:-" || key == "AGENCY:-" || key.isBlank()) {
            holder.value.visibility = View.INVISIBLE
            holder.key.text = key
            holder.divider.visibility = View.INVISIBLE
            holder.key.textSize = 12f
        } else if (key == "Contact") {
            holder.key.text = key
            holder.value.text = value
        } else {
            holder.key.text = key
            holder.value.text = value
        }





        if(isAdmin){
            holder.checkBox.setOnClickListener { v: View ->
                val checked = (v as CheckBox).isChecked

                val str = "$key: *$value*\n"
                if (checked) {
                    if (!sb.toString().contains(str)) {
                        sb.append(str)
                    }
                } else {
                    if (sb.toString().contains(str)) {
                        sb.delete(sb.indexOf(str), sb.indexOf(str) + str.length)
                    }
                }
            }

            holder.key.setOnClickListener { v: View? ->
                val checked = holder.checkBox.isChecked
                val str = "$key: *$value*\n"
                if (!checked) {
                    holder.checkBox.isChecked = true
                    if (!sb.toString().contains(str)) {
                        sb.append(str)
                    }
                } else {
                    holder.checkBox.isChecked = false
                    if (sb.toString().contains(str)) {
                        sb.delete(sb.indexOf(str), sb.indexOf(str) + str.length)
                    }
                }
            }

            holder.value.setOnClickListener {
                val checked = holder.checkBox.isChecked
                val str = "$key: *$value*\n"
                if (!checked) {
                    holder.checkBox.isChecked = true
                    if (!sb.toString().contains(str)) {
                        sb.append(str)
                    }
                } else {
                    holder.checkBox.isChecked = false
                    if (sb.toString().contains(str)) {
                        sb.delete(sb.indexOf(str), sb.indexOf(str) + str.length)
                    }
                }
            }
        }
    }
     private fun contact(number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$number")
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyDetails(context: Context, message: String?) {
        val text = if (message.isNullOrBlank()) {
            sb.toString()
        } else {
            message
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "Copied Text",
            text
        )
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }

    fun whatsapp(context: Context, message: String?) {
        val text = if(message.isNullOrBlank()){
            sb.toString()
        }else{
            message
        }

        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_TEXT, text)
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            Toast.makeText(context, "Please install WhatsApp.", Toast.LENGTH_SHORT)
                .show()
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateShowCheckBox(show: Boolean) {
        showCheckBox = show
        notifyDataSetChanged()
    }
    interface OnClickListener{
        fun onClick(value: String)
    }
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

}
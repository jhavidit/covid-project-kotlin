package com.dsckiet.covidtracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_view.view.*

class PendingListAdapter(ctx: Context, private val listItem: List<TempDataClass>) :
    RecyclerView.Adapter<PendingListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = listItem[position]
        holder.patientId.text = currentItem.patientId
        holder.patientAge.text = currentItem.patientAge.toString()
        holder.patientName.text = currentItem.patientName
    }

    override fun getItemCount(): Int = listItem.size

    class ViewHolder(unitView: View) : RecyclerView.ViewHolder(unitView) {
        val patientId: TextView = unitView.patient_id
        val patientName: TextView = unitView.patient_name
        val patientAge: TextView = unitView.patient_age
    }
}
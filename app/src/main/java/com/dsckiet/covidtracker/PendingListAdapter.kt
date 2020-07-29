package com.dsckiet.covidtracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.dsckiet.covidtracker.model.PendingPatient
import kotlinx.android.synthetic.main.item_view.view.*

class PendingListAdapter(val ctx: Context, private val listItem: List<PendingPatient>) :
    RecyclerView.Adapter<PendingListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = listItem[position]
        holder.patientId.text = currentItem.caseId
        holder.patientAge.text = currentItem.age.toString()
        holder.patientName.text = currentItem.name
        holder.patientCard.setOnClickListener {
            val patientId = holder.patientId.text.toString()
            val patientName = holder.patientName.text.toString()
            val patientAge = holder.patientAge.text.toString()
            val patientContact = currentItem.phone
            val patientGender = currentItem.gender
            val patientAddress = currentItem.address
            val patientLabName = currentItem.labName
            val bundle = bundleOf(
                "id" to patientId,
                "name" to patientName,
                "age" to patientAge,
                "contact" to patientContact,
                "gender" to patientGender,
                "address" to patientAddress,
                "labName" to patientLabName
            )

//            it.findNavController().navigate(
//                R.id.action_diagnosisPendingFragment_to_patientDetailsFragment, bundle
//            )
        }
    }

    override fun getItemCount(): Int = listItem.size

    class ViewHolder(unitView: View) : RecyclerView.ViewHolder(unitView) {
        val patientId: TextView = unitView.patient_id
        val patientName: TextView = unitView.patient_name
        val patientAge: TextView = unitView.patient_age
        val patientCard: CardView = unitView.patient_card
    }
}
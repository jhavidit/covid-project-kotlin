package com.dsckiet.covidtracker

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dsckiet.covidtracker.model.PatientDetails
import kotlinx.android.synthetic.main.item_view.view.*

class PatientDeclinedAdapter(val ctx: Context, private val patientDeclinedData: ArrayList<PatientDetails>) :
    RecyclerView.Adapter<PatientDeclinedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = patientDeclinedData[position]
        holder.patientId.text = currentItem.patientId?.substring(0, 5) + "..."
        holder.patientAge.text = currentItem.age.toString()
        holder.patientName.text = currentItem.name.toString()
        holder.patientCard.setOnClickListener {
            val patientId = holder.patientId.text.toString()
            val patientName = holder.patientName.text.toString()
            val patientAge = holder.patientAge.text.toString()
            val patientContact = currentItem.phoneNo
            val patientGender = currentItem.gender
            val patientAddress = currentItem.address
            val patientLabName = currentItem.lab
            val patientDistrict = currentItem.district
            val bundle = bundleOf(
                "id" to patientId,
                "name" to patientName,
                "age" to patientAge,
                "contact" to patientContact,
                "gender" to patientGender,
                "address" to patientAddress,
                "labName" to patientLabName,
                "district" to patientDistrict
            )
            ctx.startActivity(Intent(ctx, PatientDetailsActivity::class.java).putExtra("patientData", bundle))
        }
    }

    override fun getItemCount(): Int = patientDeclinedData.size

    class ViewHolder(unitView: View) : RecyclerView.ViewHolder(unitView) {
        val patientId: TextView = unitView.patient_id
        val patientName: TextView = unitView.patient_name
        val patientAge: TextView = unitView.patient_age
        val patientGender: TextView = unitView.patient_gender
        val patientPhoneNo: TextView = unitView.patient_phone_no
        val patientDistrict: TextView = unitView.patient_district
        val patientAddress: TextView = unitView.patient_address
        val patientLab: TextView = unitView.patient_assigned_lab
        val patientCard: CardView = unitView.patient_card
    }
}
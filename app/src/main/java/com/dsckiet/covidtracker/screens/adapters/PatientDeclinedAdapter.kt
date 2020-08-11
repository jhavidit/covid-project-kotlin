package com.dsckiet.covidtracker.screens.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.model.PatientDetails
import com.dsckiet.covidtracker.screens.ui.PatientDetailsActivity
import com.dsckiet.covidtracker.utils.InternetConnectivity
import kotlinx.android.synthetic.main.item_view.view.*

class PatientDeclinedAdapter(val ctx: Context, private val patientDeclinedData: ArrayList<PatientDetails>) :
    RecyclerView.Adapter<PatientDeclinedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = patientDeclinedData[position]
        holder.patientId.text = currentItem.caseId.toString()
        holder.patientAge.text = currentItem.age.toString()
        holder.patientName.text = currentItem.name.toString()

        //sending patient details to patient details activity
        holder.patientCard.setOnClickListener {
            val isDeclined=true
            val patientId = currentItem.patientId.toString()
            val patientName = holder.patientName.text.toString()
            val patientAge = holder.patientAge.text.toString()
            val patientContact = currentItem.phoneNo.toString()
            val patientGender = currentItem.gender.toString()
            val patientAddress = currentItem.address.toString()
            val patientLabName = currentItem.lab.toString()
            val patientDistrict = currentItem.district.toString()
            val caseId=currentItem.caseId.toString()
            val bundle = bundleOf(
                "id" to patientId,
                "name" to patientName,
                "age" to patientAge,
                "contact" to patientContact,
                "gender" to patientGender,
                "address" to patientAddress,
                "labName" to patientLabName,
                "district" to patientDistrict,
                "caseId" to caseId,
                "isDeclined" to isDeclined,
                "pageToken" to "1"
            )
            if(!InternetConnectivity.isNetworkAvailable(ctx)!!)
                Toast.makeText(ctx,"Internet unavailable", Toast.LENGTH_LONG).show()
            else
            ctx.startActivity(Intent(ctx, PatientDetailsActivity::class.java).putExtra("patientData", bundle))
        }
    }

    override fun getItemCount(): Int = patientDeclinedData.size

    class ViewHolder(unitView: View) : RecyclerView.ViewHolder(unitView) {
        val patientId: TextView = unitView.patient_id
        val patientName: TextView = unitView.patient_name
        val patientAge: TextView = unitView.patient_age
        val patientCard: CardView = unitView.patient_card
    }
}
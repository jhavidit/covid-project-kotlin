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

class PatientDeclinedAdapter(ctx: Context) : RecyclerView.Adapter<PatientDeclinedAdapter.ViewHolder>() {
    private var patientDeclinedData: List<PatientDetails> = ArrayList()
    fun setPatientData(patientList: List<PatientDetails>) {
        this.patientDeclinedData = patientList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = patientDeclinedData[position]
        holder.patientId.text = currentItem.patientId.substring(0, 5) + "..."
        holder.patientAge.text = currentItem.age.toString()
        holder.patientName.text = currentItem.name.toString()
        holder.patientAddress.text = currentItem.address
        holder.patientDistrict.text = currentItem.district
        holder.patientGender.text = currentItem.gender
        holder.patientLab.text = currentItem.lab.name.toString()
        holder.patientPhoneNo.text = currentItem.phoneNo.toString()
        val arrayList: ArrayList<String> = ArrayList()
        arrayList.add(holder.patientId.text.toString())
        arrayList.add(holder.patientName.text.toString())
        holder.patientCard.setOnClickListener {
            //ctx.startActivity(Intent(ctx, PatientDetailsActivity::class.java).putExtra("abcd", arrayList))
//            it.findNavController().navigate(R.id.action_patientDeclinedFragment_to_patientDetailsFragment,
//                bundleOf("page_token" to PAGETOKEN.toString(), "id" to holder.patientId.text.toString(),
//                "age" to holder.patientAge.text.toString(), "name" to holder.patientName.text.toString(), "address" to holder.patientAddress.text.toString(),
//                "district" to holder.patientDistrict.text.toString(), "gender" to holder.patientGender.text.toString(), "lab" to holder.patientLab.text.toString(),
//                "phoneNo" to holder.patientPhoneNo.text.toString()))
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
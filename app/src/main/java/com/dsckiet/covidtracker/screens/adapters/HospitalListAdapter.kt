package com.dsckiet.covidtracker.screens.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.model.AssignPatient
import com.dsckiet.covidtracker.model.AvailableHospital
import com.dsckiet.covidtracker.model.ChangeHospital
import com.dsckiet.covidtracker.network.PatientsApi
import com.dsckiet.covidtracker.screens.ui.MainActivity
import com.dsckiet.covidtracker.utils.logs
import kotlinx.android.synthetic.main.hospital_list.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HospitalListAdapter(
    val ctx: Context, private val hospitalList: ArrayList<AvailableHospital>,
    private val assignedHospital: String, private val patientId: String
) :
    RecyclerView.Adapter<HospitalListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.hospital_list, parent, false)
        return ViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = hospitalList[position]
        val hospitalNameAddress = currentItem.hospitalName + ", " + currentItem.hospitalAddress
        val hospitalId = currentItem.hospitalId
        holder.hospitalName.text = hospitalNameAddress
        holder.hospitalCard.setOnClickListener {
            val warning = AlertDialog.Builder(ctx)
            warning.setTitle(Html.fromHtml("<font color='#008DB9'>Are you sure want to change hospital </font>"))
                .setIcon(R.drawable.ic_profile)
                .setPositiveButton("Yes") { dialog, which ->
                    setHospital(hospitalId, assignedHospital)
                    dialog.dismiss()
                }.setNeutralButton("No") { dialog, which ->
                    dialog.dismiss()
                }
            val dialog: AlertDialog = warning.create()
            dialog.show()

        }


    }

    override fun getItemCount(): Int = hospitalList.size

    class ViewHolder(unitView: View) : RecyclerView.ViewHolder(unitView) {
        val hospitalName: TextView = unitView.hospital_name_address
        val hospitalCard: CardView = unitView.hospital_card

    }

    private fun setHospital(hospitalId: String, assignedHospital: String) {
        val changeHospital =
            ChangeHospital(assignedHospital, hospitalId)
        logs("body ${changeHospital.toString()}")
        val tokenManager = TokenManager(ctx)
        PatientsApi.retrofitService.changeHospital(
            token = tokenManager.getAuthToken().toString(),
            patientId = patientId,
            changeHospital = changeHospital
        )
            .enqueue(object : Callback<AssignPatient> {
                override fun onFailure(
                    call: Call<AssignPatient>,
                    t: Throwable
                ) {
                    logs("changeHospital ${t.message}")
                }

                override fun onResponse(
                    call: Call<AssignPatient>,
                    response: Response<AssignPatient>
                ) {
                    if (response.code() == 200) {
                        Toast.makeText(ctx, "Hospital Updated Successfully", Toast.LENGTH_LONG)
                            .show()
                        ctx.startActivity(Intent(ctx, MainActivity::class.java))
                    } else {
                        val jsonObject = JSONObject(response.errorBody()?.string()!!)

                        Toast.makeText(
                            ctx,
                            jsonObject.getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

            })
    }
}

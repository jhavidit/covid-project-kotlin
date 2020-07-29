package com.dsckiet.covidtracker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.dsckiet.covid_project_demo.SocketInstance
import com.dsckiet.covidtracker.databinding.FragmentDiagnosisPendingBinding
import com.dsckiet.covidtracker.model.PendingPatient
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class DiagnosisPendingFragment : Fragment() {
    private var mSocket: Socket? = null
    private var data: JSONObject? = null
    private var patientData: JSONArray? = null
    lateinit var list: ArrayList<PendingPatient>


    private lateinit var binding: FragmentDiagnosisPendingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_diagnosis_pending, container, false)
        return binding.root
    }


    private fun generatePendingPatientList(data: JSONArray): ArrayList<PendingPatient> {
        val list = ArrayList<PendingPatient>()

        for (i in 0 until data.length()) {
            list += PendingPatient(
                data.getJSONObject(i).getString("name"),
                data.getJSONObject(i).getString("caseId"),
                data.getJSONObject(i).getInt("age"),
                data.getJSONObject(i).getString("gender"),
                data.getJSONObject(i).getString("phone"),
                data.getJSONObject(i).getString("address"),
                data.getJSONObject(i).getJSONObject("lab").getString("name"),
                data.getJSONObject(i).getString("district")
            )
        }
        return list
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val app: SocketInstance = activity?.application as SocketInstance
        mSocket = app.getMSocket() //socket instance

        mSocket?.connect()
        val options = IO.Options()
        options.reconnection = true
        options.forceNew = true

        socketConnection()
    }

    private fun socketConnection() {
        val jsonObject1 = JSONObject() //tocken object
        try {
            jsonObject1.put(
                "token",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjVmMTMzZWEzMTgxOTM2MWUxMDE2Y2U5NCIsIm5hbWUiOiJSb290IERvY3RvciIsImVtYWlsIjoicmF3aWI1NDQzMkBleHBsb3JheGIuY29tIiwicm9sZSI6ImRvY3RvciIsImlhdCI6MTU5NTA5NjkzOX0.Ci50Rfi8oW6BlxIS8IP4329JQEBMDdoFyWex1iq7_sM"
            )

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mSocket!!.on("PATIENTS_POOL_FOR_DOCTOR") { args ->    //requesting patient details
            if (args[0] != null)
                data = args[0] as JSONObject

            if (data != null) {
                Log.d("test", data.toString())
                patientData = data!!.getJSONArray("patients")
                list = generatePendingPatientList(patientData!!)
                activity?.runOnUiThread {
                    binding.animationView.visibility = GONE
                    binding.recyclerView.visibility = VISIBLE
                    binding.diagnosisPendingCount.text = data!!.getString("remainingPatients")
                    binding.recyclerView.adapter = DiagnosisPendingAdapter(requireContext(), list)
                }
            } else {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "No patient available", Toast.LENGTH_SHORT)
                        .show()
                }
            }


        }.on(Socket.EVENT_DISCONNECT) { args ->
            activity?.runOnUiThread {
                binding.recyclerView.visibility = GONE
                binding.animationView.visibility = VISIBLE
                binding.diagnosisPendingCount.text = ""
                Toast.makeText(requireContext(), "Internet Unavailable", Toast.LENGTH_SHORT).show()
            }

        }.on(Socket.EVENT_RECONNECT) { args ->
            try {
                mSocket?.emit(
                    "patientsPoolForDoctor", jsonObject1
                )
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }

        try {
            mSocket?.emit(
                "patientsPoolForDoctor", jsonObject1
            )
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()


    }


}
package com.dsckiet.covidtracker.screens.ui

import android.annotation.SuppressLint
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
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.screens.adapters.DiagnosisPendingAdapter
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.databinding.FragmentDiagnosisPendingBinding
import com.dsckiet.covidtracker.model.PendingPatient
import com.dsckiet.covidtracker.utils.InternetConnectivity
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_diagnosis_pending.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.koin.android.ext.android.bind
import java.net.BindException
import java.net.URISyntaxException

class DiagnosisPendingFragment : Fragment() {
    private var mSocket: Socket? = null
    private var data: JSONObject? = null
    private var patientData: JSONArray? = null
    private lateinit var tokenManager: TokenManager
    lateinit var list: ArrayList<PendingPatient>


    private lateinit var binding: FragmentDiagnosisPendingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater,
                R.layout.fragment_diagnosis_pending, container, false)
        return binding.root
    }

    @SuppressLint("LogNotTimber")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())
        if(!InternetConnectivity.isNetworkAvailable(requireContext())!!) {
            Snackbar.make(binding.coordinatorLayout,"Internet Unavailable",Snackbar.LENGTH_SHORT).show()
            binding.animationView.visibility=GONE
        }
    }
    private fun generatePendingPatientList(data: JSONArray): ArrayList<PendingPatient> {
        val list = ArrayList<PendingPatient>()


        for (i in 0 until data.length()) {
            try {
                list += PendingPatient(
                    data.getJSONObject(i).getString("name"),
                    data.getJSONObject(i).getString("_id"),
                    data.getJSONObject(i).getInt("age"),
                    data.getJSONObject(i).getString("gender"),
                    data.getJSONObject(i).getString("phone"),
                    data.getJSONObject(i).getString("address"),
                    data.getJSONObject(i).getString("lab"),
                    data.getJSONObject(i).getString("district"),
                    data.getJSONObject(i).getString("caseId")

                )
            } catch (e: JSONException) {
                println("json exception : ${e.message}")
            }
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
        val token=tokenManager.getAuthToken()
        val jsonObject1 = JSONObject() //token object
        try {
            jsonObject1.put(
                "token",
                token )

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mSocket!!.on("PATIENTS_POOL_FOR_DOCTOR") { args ->    //requesting patient details
            if (args[0] != null)
                data = args[0] as JSONObject

            if (data != null) {
                if (data.toString() != "{}" || data.toString() != "[]") {
                    patientData = data!!.getJSONArray("patients")
                    list = generatePendingPatientList(patientData!!)
                    activity?.runOnUiThread {
                        binding.animationView.visibility = GONE
                        binding.recyclerView.visibility = VISIBLE
                        binding.diagnosisPendingCount.text = data!!.getString("remainingPatients")
                        binding.recyclerView.adapter =
                            DiagnosisPendingAdapter(
                                requireContext(),
                                list
                            )
                    }
                } else {
                    activity?.runOnUiThread {
                        binding.animationView.visibility=GONE
                        Snackbar.make(coordinator_layout,"No available Patient",Snackbar.LENGTH_SHORT)
                    }
                }
            }


        }.on(Socket.EVENT_DISCONNECT) { args->
            activity?.runOnUiThread {
                binding.recyclerView.visibility = GONE
                binding.animationView.visibility = GONE
                binding.diagnosisPendingCount.text = "0"
                Snackbar.make(binding.coordinatorLayout,"Some problem occurred check your network connection or restart the app",Snackbar.LENGTH_SHORT).show()
            }

        }.on(Socket.EVENT_RECONNECT) {args->
            try {
                mSocket?.connect()
                mSocket?.emit(
                    "patientsPoolForDoctor", jsonObject1
                )
                activity?.runOnUiThread{
                    binding.animationView.visibility= VISIBLE
                }
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
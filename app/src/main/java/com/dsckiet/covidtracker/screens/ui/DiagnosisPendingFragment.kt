package com.dsckiet.covidtracker.screens.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.databinding.FragmentDiagnosisPendingBinding
import com.dsckiet.covidtracker.model.PendingPatient
import com.dsckiet.covidtracker.network.SocketInstance
import com.dsckiet.covidtracker.screens.adapters.DiagnosisPendingAdapter
import com.dsckiet.covidtracker.utils.InternetConnectivity
import com.dsckiet.covidtracker.utils.logs
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.snackbar.Snackbar
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class DiagnosisPendingFragment : Fragment() {
    private var mSocket: Socket? = null
    private var data: JSONObject? = null
    private var patientData: JSONArray? = null
    private lateinit var tokenManager: TokenManager
    private lateinit var list: ArrayList<PendingPatient>
    private lateinit var tickerView: TickerView

    private lateinit var binding: FragmentDiagnosisPendingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_diagnosis_pending, container, false
            )
        tickerView = binding.diagnosisPendingCount
        tickerView.animationInterpolator = OvershootInterpolator()
        tickerView.setCharacterLists(TickerUtils.provideNumberList())
        val fontFace = ResourcesCompat.getFont(requireContext(), R.font.sen_bold)
        tickerView.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY)
        tickerView.typeface = fontFace
        tickerView.gravity = Gravity.START
        tickerView.animationDuration = 2000

        binding.openSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManager = TokenManager(requireContext())
        if (!InternetConnectivity.isNetworkAvailable(requireContext())!!) {
            offlineCase()
        }
    }

    private fun offlineCase() {
        binding.animationView.setAnimation(R.raw.no_internet)
        binding.animationView.visibility = VISIBLE
        binding.openSettings.visibility = VISIBLE
        Snackbar.make(binding.coordinatorLayout, "Network Problem", Snackbar.LENGTH_LONG)
            .setAction("Open Settings") {
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }.show()
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

        startSocketConnection()
    }

    private fun startSocketConnection() {
        val token = tokenManager.getAuthToken()
        val jsonObjectToken = JSONObject() //token object
        try {
            jsonObjectToken.put(
                "token",
                token
            )

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        mSocket!!.on("PATIENTS_POOL_FOR_DOCTOR") { args ->    //requesting patient details
            if (args[0] != null)
                data = args[0] as JSONObject

            if (data != null) {
                //to check size of data
                logs(data!!.length().toString())
                if (data.toString() != "{}" || data.toString() != "[]") {
                    patientData = data!!.getJSONArray("patients")
                    list = generatePendingPatientList(patientData!!)
                    activity?.runOnUiThread {

                        binding.animationView.visibility = GONE
                        binding.openSettings.visibility = GONE
                        binding.recyclerView.visibility = VISIBLE

                        tickerView.text = data!!.getString("remainingPatients")
                        binding.recyclerView.adapter =
                            DiagnosisPendingAdapter(
                                requireContext(),
                                list
                            )
                    }
                } else {
                    //TODO:: Need Testing of empty list data
                    activity?.runOnUiThread {
                        binding.animationView.setAnimation(R.raw.empty_list)
                        binding.animationView.visibility = VISIBLE
                        binding.openSettings.visibility = GONE

                        Snackbar.make(
                            binding.coordinatorLayout,
                            "No available Patient",
                            Snackbar.LENGTH_LONG
                        )
                    }
                }
            }

        }.on(Socket.EVENT_DISCONNECT) {
            activity?.runOnUiThread {
                binding.recyclerView.visibility = GONE

                if (InternetConnectivity.isNetworkAvailable(requireContext()) == false) {
                    offlineCase()
                } else {
                    binding.animationView.setAnimation(R.raw.heartbeat_loading)
                    binding.animationView.visibility = VISIBLE
                    binding.openSettings.visibility = GONE
                }

                tickerView.text = "0"
                Snackbar.make(binding.coordinatorLayout, "Network Problem", Snackbar.LENGTH_LONG)
                    .setAction("Open Settings") {
                        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    }.show()
            }

        }.on(Socket.EVENT_RECONNECT) {
            try {
                mSocket?.connect()
                mSocket?.emit(
                    "patientsPoolForDoctor", jsonObjectToken
                )
                activity?.runOnUiThread {
                    if (InternetConnectivity.isNetworkAvailable(requireContext()) == false) {
                        offlineCase()
                    } else {
                        binding.animationView.setAnimation(R.raw.heartbeat_loading)
                        binding.animationView.playAnimation()
                        binding.animationView.repeatMode
                        binding.animationView.visibility = VISIBLE
                        binding.openSettings.visibility = GONE
                    }
                }
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }

        try {
            mSocket?.emit(
                "patientsPoolForDoctor", jsonObjectToken
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
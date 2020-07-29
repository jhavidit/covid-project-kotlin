package com.dsckiet.covidtracker

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.model.PatientDetails
import com.dsckiet.covidtracker.model.ResponseModel
import com.dsckiet.covidtracker.network.PatientsApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PatientDeclinedViewModel: ViewModel() {
    private val  _responseModel = MutableLiveData<ResponseModel> ()
    val responseModel: LiveData<ResponseModel>
    get() = _responseModel

    private val _patientDeclinedData = MutableLiveData<List<PatientDetails>> ()
    val patientDeclinedData : LiveData<List<PatientDetails>>
    get() = _patientDeclinedData

    private val _countListItem = MutableLiveData<String> ()
    val countListItem : LiveData<String>
    get() = _countListItem

    init {
        getPatientDeclinedData()
    }

    companion object {
        private val auth_token: String = TokenManager.USER_TOKEN
    }

    private fun getPatientDeclinedData () {
        PatientsApi.retrofitService.getPatientsData(token = auth_token)
            .enqueue(
                object : Callback<ResponseModel> {
                    @SuppressLint("LogNotTimber")
                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        Log.d("onFailure response","Network request failed with error: ${t.message}")
                    }

                    @SuppressLint("LogNotTimber")
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        _responseModel.value = response.body()
                        Log.d("fetch_status", "response code = ${response.code()} & response body = ${response.body()}")
                        _patientDeclinedData.value = responseModel.value?.data
                        _countListItem.value = patientDeclinedData.value?.size.toString()
                        Log.d("fetch_success","fetch_success, response: ${_patientDeclinedData.value.toString()}")
                    }
                }
            )
    }
}
package com.dsckiet.covidtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.databinding.FragmentPatientDeclinedBinding
import kotlinx.android.synthetic.main.fragment_patient_declined.*


class PatientDeclinedFragment : Fragment() {

    private lateinit var binding: FragmentPatientDeclinedBinding
    private val viewModel: PatientDeclinedViewModel by lazy {
        ViewModelProviders.of(this).get(PatientDeclinedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_patient_declined, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter: PatientDeclinedAdapter = PatientDeclinedAdapter(requireContext())
        recycler_view.adapter = adapter
        viewModel.patientDeclinedData.observe(viewLifecycleOwner, Observer {
            adapter.setPatientData(it)
        })
        viewModel.countListItem.observe(viewLifecycleOwner, Observer {
            binding.countDecPatients.text = it.toString()
        })
    }
}

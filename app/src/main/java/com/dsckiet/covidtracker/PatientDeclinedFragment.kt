package com.dsckiet.covidtracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsckiet.covidtracker.databinding.FragmentPatientDeclinedBinding


class PatientDeclinedFragment : Fragment() {
    private lateinit var binding: FragmentPatientDeclinedBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_patient_declined, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = generateDemoListData()
        binding.recyclerView.adapter = PatientDeclinedAdapter(requireActivity(), list)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
    }
    private fun generateDemoListData(): List<TempDataClass> {
        val list = ArrayList<TempDataClass>()
        list += TempDataClass("P1A56", "Rohan Mehta", 62)
        list += TempDataClass("P1A02", "Anshul Gupta", 32)
        list += TempDataClass("P1A51", "Aakanksha Shivani", 30)
        list += TempDataClass("P1A46", "Shubham Goswami", 96)
        list += TempDataClass("P1A91", "Mayank Shakya", 14)
        list += TempDataClass("P1A78", "Aditya Singh", 43)
        list += TempDataClass("P1A01", "Nidhaan Srivastava", 54)
        return list
    }
}
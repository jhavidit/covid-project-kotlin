package com.dsckiet.covidtracker

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dsckiet.covidtracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var navController: NavController
    private var previousNavigationPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.NavHostFragment)

        binding.bottomNav.setTypeface(Typeface.DEFAULT_BOLD)
        initBottomNav()
    }

    private fun initBottomNav() {
        binding.bottomNav.setNavigationChangeListener{_, position ->
            if(previousNavigationPosition != position){
                when(position){
                    0 -> {
                        binding.bottomNav.setCurrentActiveItem(0)
                        if(previousNavigationPosition == 1) {
                            navController.navigate(R.id.action_diagnosisCompleteFragment_to_diagnosisPendingFragment)
                        } else {
                            navController.navigate(R.id.action_profileFragment_to_diagnosisPendingFragment)
                        }
                        previousNavigationPosition = position
                    }
                    1 -> {
                        binding.bottomNav.setCurrentActiveItem(1)
                        if(previousNavigationPosition == 0) {
                            navController.navigate(R.id.action_diagnosisPendingFragment_to_diagnosisCompleteFragment)
                        } else {
                            navController.navigate(R.id.action_profileFragment_to_diagnosisCompleteFragment)
                        }
                        previousNavigationPosition = position
                    }
                    2 -> {
                        binding.bottomNav.setCurrentActiveItem(2)
                        if(previousNavigationPosition == 0) {
                            navController.navigate(R.id.action_diagnosisPendingFragment_to_profileFragment)
                        } else {
                            navController.navigate(R.id.action_diagnosisCompleteFragment_to_profileFragment)
                        }
                        previousNavigationPosition = position
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if(previousNavigationPosition != 0) {
            binding.bottomNav.setCurrentActiveItem(0)
            previousNavigationPosition = 0
            navController.popBackStack(R.id.diagnosisPendingFragment, false)
        } else {
            // For back press, code snackBar here
            finish()
        }
    }
}
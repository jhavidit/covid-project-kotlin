package com.dsckiet.covidtracker.screens.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var previousNavigationPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )
        navController = Navigation.findNavController(this,
            R.id.NavHostFragment
        )

        binding.bottomNav.setTypeface(Typeface.DEFAULT_BOLD)
        initBottomNav()
        checkPermission()
    }

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CALL_PHONE
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    42
                )
            }
        } else {
            // Permission has already been granted

        }
    }

    //TODO:: Needs to be update permissions checking
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == 42) {
            // If request is cancelled, the result arrays are empty.
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted, yay!

            } else {
                // permission denied, boo! Disable the
                // functionality
            }
            return
        }
    }

    private fun initBottomNav() {
        binding.bottomNav.setNavigationChangeListener { _, position ->
            if (previousNavigationPosition != position) {
                when (position) {
                    0 -> {
                        binding.bottomNav.setCurrentActiveItem(0)
                        if (previousNavigationPosition == 1) {
                            navController.navigate(R.id.action_patientDeclinedFragment_to_diagnosisPendingFragment)
                        } else {
                            navController.navigate(R.id.action_profileFragment_to_diagnosisPendingFragment)
                        }
                        previousNavigationPosition = position
                    }
                    1 -> {
                        binding.bottomNav.setCurrentActiveItem(1)
                        if (previousNavigationPosition == 0) {
                            navController.navigate(R.id.action_diagnosisPendingFragment_to_patientDeclinedFragment)
                        } else {
                            navController.navigate(R.id.action_profileFragment_to_patientDeclinedFragment)
                        }
                        previousNavigationPosition = position
                    }
                    2 -> {
                        binding.bottomNav.setCurrentActiveItem(2)
                        if (previousNavigationPosition == 0) {
                            navController.navigate(R.id.action_diagnosisPendingFragment_to_profileFragment)
                        } else {
                            navController.navigate(R.id.action_patientDeclinedFragment_to_profileFragment)
                        }
                        previousNavigationPosition = position
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (previousNavigationPosition != 0) {
            binding.bottomNav.setCurrentActiveItem(0)
            previousNavigationPosition = 0
            navController.popBackStack(R.id.diagnosisPendingFragment, false)
        } else {
            // For back press, code snackBar here
            finish()
        }
    }
}
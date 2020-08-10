package com.dsckiet.covidtracker.screens.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dsckiet.covidtracker.R
import com.dsckiet.covidtracker.authentication.TokenManager
import com.dsckiet.covidtracker.databinding.FragmentProfileBinding
import com.dsckiet.covidtracker.password.Password
import com.dsckiet.covidtracker.profile.ProfileAPI
import com.dsckiet.covidtracker.profile.models.ProfileResponse
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var doctorId: String
    lateinit var photoURL: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO => Increase spinner button touch area

        binding.dropdownBtn.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.dropdown_menu, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.editProfile -> {
                        val name = binding.docName.text
                        val id = binding.docId.text
                        val contact = binding.docPhoneNum.text
                        val ageGender = binding.docAgeGender.text
                        val age =
                            ageGender.substring(
                                ageGender.indexOf("|") + 2,
                                (ageGender.indexOf("y") - 1)
                            )
                        val about = binding.docProfileDetails.text
                        val address = binding.docAddressInfo.text
                        val doctorUrlId = doctorId
                        val bundle = bundleOf(
                            "name" to name,
                            "id" to id,
                            "contact" to contact,
                            "age" to age,
                            "about" to about,
                            "address" to address,
                            "doctorId" to doctorUrlId,
                            "image" to photoURL

                        )
                        val intent = Intent(requireContext(), ProfileUpdateActivity::class.java)
                        intent.putExtra("doctorDetails", bundle)
                        startActivity(intent)
                        true
                    }
                    R.id.changePwd -> {
                        val i = Intent(requireContext(), Password::class.java)
                        startActivity(i)
                        true
                    }
                    R.id.logout -> {
                        logout()
                        true
                    }
                    else -> {
                        true
                    }
                }
            }
        }

        tokenManager = TokenManager(requireContext())
        val token = tokenManager.getAuthToken()
        if (token != null) {

            ProfileAPI.retrofitService.getProfile(token)
                .enqueue(object : Callback<ProfileResponse> {
                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        binding.animationView.visibility = GONE
                        Snackbar.make(
                            binding.coordinatorLayout,
                            "Network Problem",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {
                        binding.relativeLayoutProfile.visibility = VISIBLE
                        binding.animationView.visibility = GONE
                        if (response.code() == 200) {
                            val profile = response.body()
                            if (profile?.message == "success" && !profile.error) {
                                binding.docId.text = profile.data?.empId
                                binding.docName.text = profile.data?.name
                                Glide.with(requireContext()).load(profile.data?.image)
                                    .into(binding.docPhoto)
                                doctorId = profile.data?._id.toString()
                                binding.docAddressInfo
                                val age = profile.data?.age
                                val gender = profile.data?.gender
                                photoURL = profile.data?.image.toString()
                                binding.docAgeGender.text = "$gender | $age years" as String
                                binding.docProfileDetails.text = profile.data?.about
                                binding.docPhoneNum.text = profile.data?.contact
                                binding.docAddressInfo.text = profile.data?.address
                                binding.docHospitalInfo.text = profile.data?.hospital

                            }
                        } else {
                            val jsonObject = JSONObject(response.errorBody()?.string()!!)

                            Snackbar.make(
                                binding.coordinatorLayout,
                                jsonObject.getString("message"),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }

    }


    private fun logout() {
        val warning = AlertDialog.Builder(requireContext())
        warning.setTitle("Log out")
            .setMessage("Do you want to logout?")
            .setIcon(R.drawable.ic_profile)
            .setPositiveButton("Yes") { _, _ ->
                tokenManager.deleteAuthToken()
                val i = Intent(requireContext(), LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)

            }.setNeutralButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = warning.create()
        dialog.show()
    }
}


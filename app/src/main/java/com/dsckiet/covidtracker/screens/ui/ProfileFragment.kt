package com.dsckiet.covidtracker.screens.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dsckiet.covidtracker.Authentication.TokenManager
import com.dsckiet.covidtracker.Profile.Models.ProfileResponse
import com.dsckiet.covidtracker.Profile.ProfileAPI
import com.dsckiet.covidtracker.databinding.FragmentProfileBinding
import kotlinx.android.synthetic.main.fragment_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.dsckiet.covidtracker.R
import android.os.Build
import android.text.Html
import android.view.*
import android.view.View.GONE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import com.dsckiet.covidtracker.Profile.Models.NewPasswordRequest
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.bind
import android.view.View.VISIBLE as VISIBLE


class ProfileFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var tokenManager: TokenManager
    var doctorId:String=""

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
        val array = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Spinner,
            R.layout.ghost_spinner
        )
        array.setDropDownViewResource(R.layout.spinner)
        spinner.adapter = array
        spinner.onItemSelectedListener = this


        tokenManager = TokenManager(requireContext())
        val token = tokenManager.getAuthToken()
        if (token != null) {

            ProfileAPI.retrofitService.getProfile(token)
                .enqueue(object : Callback<ProfileResponse> {
                    @SuppressLint("LogNotTimber")
                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        binding.animationView.visibility=GONE
                        Snackbar.make(
                            binding.coordinatorLayout,
                            "Some problem occurred check your network connection or restart the app",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {
                        binding.relativeLayoutProfile.visibility=VISIBLE
                        binding.animationView.visibility=GONE
                        val profile = response.body()
                        if (profile?.message == "success" && !profile.error) {
                            binding.docId.text = profile.data.empId
                            binding.docName.text = profile.data.name
                            Glide.with(requireContext()).load(profile.data.image).into(binding.docPhoto)
                            doctorId = profile.data._id
                            binding.docAddressInfo
                            val age=profile.data.age
                            val gender=profile.data.gender
                            binding.docAgeGender.text="$gender | $age years"
                            binding.docProfileDetails.text=profile.data.about
                            binding.docPhoneNum.text=profile.data.contact
                            binding.docAddressInfo.text=profile.data.address
                            binding.docHospitalInfo.text=profile.data.hospital

                        }
                    }
                })
        }

    }
    //TODO-comment -> next to implemented fun for spinner
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when (position) {

            0 -> {

            }
            1 -> {
                val name = binding.docName.text
                val id=binding.docId.text
                val contact=binding.docPhoneNum.text
                val ageGender=binding.docAgeGender.text
                val age=ageGender.substring(ageGender.indexOf("|")+2,(ageGender.indexOf("y")-1))
                val about=binding.docProfileDetails.text
                val address = binding.docAddressInfo.text
                val doctorUrlId=doctorId
                val bundle= bundleOf("name" to name,"id" to id,"contact" to contact,"age" to age,"about" to about,"address" to address,"doctorId" to doctorUrlId)
                val intent= Intent(requireContext(), ProfileUpdateActivity::class.java)
                intent.putExtra("doctorDetails",bundle)
                startActivity(intent)
                spinner.setSelection(0)
            }
            2 -> {

            }
            3->{
                Logout()
            }
        }
    }


    private fun Logout() {
        val warning = AlertDialog.Builder(requireContext())
        warning.setTitle(Html.fromHtml("<font color='#008DB9'>Do you want to logout?</font>"))
            .setIcon(R.drawable.ic_profile)
            .setPositiveButton("Yes") { dialog, which ->
                tokenManager.deleteAuthToken()
                val i = Intent(requireContext(), LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)

            }.setNeutralButton("No") { dialog, which ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = warning.create()
        dialog.show()
        spinner.setSelection(0)
    }
}


package com.dsckiet.covidtracker

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.dsckiet.covidtracker.Profile.Models.NewPasswordRequest
import com.dsckiet.covidtracker.screens.ui.LoginActivity


class ProfileFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var tokenManager: TokenManager

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

        //TODO-commnet->Spinner Adapeter added and spinner created
        val array = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Spinner,
            R.layout.ghost_spinner
        )
        array.setDropDownViewResource(R.layout.spinner)
        spinner.adapter = array
        spinner.onItemSelectedListener = this


        tokenManager = TokenManager(requireContext())


        ProfileAPI.retrofitService.getProfile(token = tokenManager.getAuthToken()!!)
            .enqueue(object : Callback<ProfileResponse> {
                @SuppressLint("LogNotTimber")
                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {

                    Log.i("msg", "ABCD : ${t.message}")
                    Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()

                    val profile = response.body()
                    if (profile?.message == "success" && !profile.error) {

                        doc_id.text = profile.data.empId
                        doc_name.text = profile.data.name
                        Glide.with(requireContext()).load(profile.data.image).into(doc_photo)
                    }
                }
            })
    }


    //TODO-comment -> next to implemented fun for spinner
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when (position) {
            0 -> {
                Toast.makeText(parent?.context, "clicked", Toast.LENGTH_SHORT).show()
                spinner.setSelection(0)
            }
            1 -> {

            }
            2 -> {
                Logout()
            }
        }
    }


    fun Logout() {
        val warning = AlertDialog.Builder(requireContext())
        warning.setTitle(Html.fromHtml("<font color='#008DB9'>Do you want to logout?</font>"))
            .setIcon(R.drawable.ic_profile)
            .setPositiveButton("Yes") { dialog, which ->
                tokenManager.deleteAuthToken()
                val i = Intent(requireContext(),LoginActivity::class.java)
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


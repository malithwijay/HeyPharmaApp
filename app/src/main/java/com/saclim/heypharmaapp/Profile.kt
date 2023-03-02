package com.saclim.heypharmaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_dashboard.*

class Profile : AppCompatActivity() {
    private lateinit var textProfileUsername:TextView
    private lateinit var textInputProfile:TextInputEditText
    private lateinit var textInputProfileDob:TextInputEditText
    private lateinit var textInputProfileTp:TextInputEditText
    private lateinit var textInputProfileAddress:TextInputEditText
    private lateinit var textInputProfileEmail:TextInputEditText
    private lateinit var databaseReference:DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var errorDialog:SweetAlertDialog
    private lateinit var loadingDialog: SweetAlertDialog
    private lateinit var confirmDialog: SweetAlertDialog
    private lateinit var btnProfileUpdate:MaterialButton
    private lateinit var successDialog: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(3).isChecked=true
        //bottomNavigationView.menu.getItem(2).isEnabled = false

        textProfileUsername = findViewById(R.id.textProfileUsername)
        textInputProfile = findViewById(R.id.textInputProfile)
        textInputProfileDob = findViewById(R.id.textInputProfileDob)
        textInputProfileTp = findViewById(R.id.textInputProfileTp)
        textInputProfileAddress = findViewById(R.id.textInputProfileAddress)
        textInputProfileEmail = findViewById(R.id.textInputProfileEmail)
        btnProfileUpdate = findViewById(R.id.btnProfileUpdate)


        val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Payment -> {
                    val intent= Intent(this, Payment::class.java)
                    finish()
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.Profile -> {
                    val intent= Intent(this, Profile::class.java)
                    finish()
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.Logout -> {
                    val intent= Intent(this, LoginScreen::class.java)
                    finish()
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.Home->{
                    val intent=Intent(this,DashboardHome::class.java)
                    finish()
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }

            }
            false
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        loadProfileDetails()
        btnProfileUpdate.setOnClickListener {

            showConfirmMessage("Are you sure to update your details...")
            confirmDialog.setConfirmButton("Yes",SweetAlertDialog.OnSweetClickListener {
                confirmDialog.dismissWithAnimation()
                updateMemberDetails()
            })
            confirmDialog.setCancelButton("No",SweetAlertDialog.OnSweetClickListener {
                confirmDialog.dismissWithAnimation()
            })
        }
    }
    private fun loadProfileDetails(){
        showLoadingMessage("Loading Your Details Please wait...")
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Member")
        databaseReference.child(firebaseAuth.currentUser!!.uid.toString()).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val memberDetails = snapshot.getValue<Member>()
                if(memberDetails!=null){
                    textProfileUsername.text=memberDetails.name
                    textInputProfile.setText(memberDetails.name)
                    textInputProfileDob.setText(memberDetails.dob)
                    textInputProfileTp.setText(memberDetails.telephone.toString())
                    textInputProfileAddress.setText(memberDetails.address)
                    textInputProfileEmail.setText(memberDetails.email)
                    loadingDialog.dismissWithAnimation()
                }else{
                    loadingDialog.dismissWithAnimation()
                    showErrorMessage("Unable to load member details try again...")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissWithAnimation()
                showErrorMessage("Operation Canceled...")
            }
        })
    }
    private fun updateMemberDetails(){
        showLoadingMessage("Updating your details please wait")
        val updateMember = Member(textInputProfile.text.toString(),textInputProfileDob.text.toString(),Integer.parseInt(textInputProfileTp.getText().toString()),textInputProfileAddress.text.toString(),textInputProfileEmail.text.toString())
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Member")
        databaseReference.child(firebaseAuth.currentUser!!.uid).setValue(updateMember).addOnCompleteListener { result->
            if(result.isSuccessful){
                loadingDialog.dismissWithAnimation()
                showSuccessMessage("Details Updated successfully...")
                successDialog.setConfirmButton("Ok",SweetAlertDialog.OnSweetClickListener {
                    successDialog.dismissWithAnimation()
                    val intent = Intent(this,Profile::class.java)
                    finish()
                    startActivity(intent)
                })
            }else{
                loadingDialog.dismissWithAnimation()
                showErrorMessage("Unable to Update details try again...")
            }
        }
    }
    private fun showErrorMessage(errorText:String){
        errorDialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        errorDialog.setCancelable(true)
        errorDialog.setTitleText("Error...!")
        errorDialog.setContentText(errorText)
        errorDialog.show()
    }
    private fun showLoadingMessage(message:String){
        loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText("Please Wait...")
            .setContentText(message)
        loadingDialog.setCancelable(false)
        loadingDialog.show()
    }
    private fun showConfirmMessage(message:String){
        confirmDialog = SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
        confirmDialog.setCancelable(true)
        confirmDialog.setCustomImage(R.drawable.question_mark)
        confirmDialog.setTitleText("Confirm...!")
        confirmDialog.setContentText(message)
        confirmDialog.show()
    }
    private fun showSuccessMessage(message:String){
        successDialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        successDialog.setCancelable(true)
        successDialog.setTitleText("Done...!")
        successDialog.setContentText(message)
        successDialog.show()
    }
}
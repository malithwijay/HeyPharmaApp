package com.saclim.heypharmaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.text.isDigitsOnly
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_registration_screen.*

class Payment : AppCompatActivity() {
    private lateinit var fab:FloatingActionButton
    private lateinit var errorDialog: SweetAlertDialog
    private lateinit var confirmDialog: SweetAlertDialog
    private lateinit var loadingDialog: SweetAlertDialog
    private lateinit var successDialog: SweetAlertDialog
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var textInputName: TextInputEditText
    private lateinit var textInputAddressLine1: TextInputEditText
    private lateinit var textInputAddressLine2: TextInputEditText
    private lateinit var textInputCity: TextInputEditText
    private lateinit var textInputProvince: TextInputEditText
    private lateinit var textInputTp: TextInputEditText
    private lateinit var inputName:TextInputLayout
    private lateinit var inputAddressLine1:TextInputLayout
    private lateinit var inputAddressLine2:TextInputLayout
    private lateinit var inputCity:TextInputLayout
    private lateinit var inputProvince:TextInputLayout
    private lateinit var inputAddressLine1Tp:TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(1).isChecked=true

        val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Payment -> {
                    val intent= Intent(this, Payment::class.java)
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
                R.id.Profile -> {
                    val intent= Intent(this, Profile::class.java)
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

        ////////////////////////////////////////////////////////////////////////////////////////////
        fab = findViewById(R.id.fab)
        textInputName = findViewById(R.id.textInputName)
        textInputAddressLine1 = findViewById(R.id.textInputAddressLine1)
        textInputAddressLine2 = findViewById(R.id.textInputAddressLine2)
        textInputCity = findViewById(R.id.textInputCity)
        textInputProvince = findViewById(R.id.textInputProvince)
        textInputTp = findViewById(R.id.textInputTp)
        inputName = findViewById(R.id.InputName)
        inputAddressLine1 = findViewById(R.id.InputAddressLine1)
        inputAddressLine2 = findViewById(R.id.InputAddressLine2)
        inputCity = findViewById(R.id.InputCity)
        inputProvince = findViewById(R.id.InputProvince)
        inputAddressLine1Tp = findViewById(R.id.InputAddressLine1Tp)

        clearErrorText()
        showLoadingMessage("Loading your details please wait...")
        loadOldShippingDetails()

        fab.setOnClickListener {
            val validation = Validation()
            if(textInputName.text.isNullOrEmpty()) {
                clearErrorText()
                inputName.helperText = "*Enter Your Name"
            }else if(validation.isNumeric(textInputName.text.toString())==true){
                clearErrorText()
                inputName.helperText = "*Name Cannot Have Numbers"
            }else if(textInputAddressLine1.text.isNullOrEmpty()){
                clearErrorText()
                inputAddressLine1.helperText="*Enter Address Line"
            }else if(textInputCity.text.isNullOrEmpty()){
                clearErrorText()
                inputCity.helperText="*Enter Address City"
            }else if(textInputProvince.text.isNullOrEmpty()){
                clearErrorText()
                inputProvince.helperText="*Enter Province"
            }else if(textInputTp.text.isNullOrEmpty()){
                clearErrorText()
                inputAddressLine1Tp.helperText="*Enter Shipping Telephone"
            }else if(!textInputTp.text!!.isDigitsOnly()){
                clearErrorText()
                inputAddressLine1Tp.helperText="*Telephone Number Cannot Have Letters"
            }else if(textInputTp.text!!.length<10){
                clearErrorText()
                inputAddressLine1Tp.helperText="*Telephone Number length is 10"
            }else {
                clearErrorText()
                showLoadingMessage("Saving Shipping Address Details")
                saveShippingDetails()
            }
        }
    }
    private fun loadOldShippingDetails(){
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("ShippingAddress")

        val userID:String = firebaseAuth.currentUser!!.uid

        databaseReference.child(userID).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val shipAddr = snapshot.getValue<ShippingAddress>()
                if(shipAddr!=null){
                    textInputName.setText(shipAddr.fullName)
                    textInputAddressLine1.setText(shipAddr.addressLine1)
                    textInputAddressLine2.setText(shipAddr.addressLine2)
                    textInputCity.setText(shipAddr.city)
                    textInputProvince.setText(shipAddr.province)
                    textInputTp.setText(shipAddr.shipTelephone)
                    loadingDialog.dismissWithAnimation()
                }
                else{
                    loadingDialog.dismissWithAnimation()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissWithAnimation()
                showErrorMessage("Something went wrong try again...")
            }
        })
    }
    private fun clearErrorText(){
        inputName.helperText=""
        inputAddressLine1.helperText=""
        inputAddressLine2.helperText=""
        inputCity.helperText=""
        inputProvince.helperText=""
        inputAddressLine1Tp.helperText=""
    }
    private fun fabIconChanger(){
        val validation = Validation()
        if(textInputName.text.isNullOrEmpty() || validation.isNumeric(textInputName.text.toString()) || textInputAddressLine1.text.isNullOrEmpty() || textInputCity.text.isNullOrEmpty() || textInputProvince.text.isNullOrEmpty() || textInputTp.text.isNullOrEmpty() || !textInputTp.text!!.isDigitsOnly() || textInputTp.text!!.length<10){
        }else {
            fab.setImageDrawable(resources.getDrawable(R.drawable.done_icon))
        }
    }
    private fun saveShippingDetails(){

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("ShippingAddress")

        val userID:String = firebaseAuth.currentUser!!.uid
        val shipAdd = ShippingAddress(textInputName.text.toString(),textInputAddressLine1.text.toString(),textInputAddressLine2.text.toString(),textInputCity.text.toString(),textInputProvince.text.toString(),textInputTp.text.toString())

        databaseReference.child(userID).setValue(shipAdd).addOnCompleteListener { result->
            if(result.isSuccessful){
                loadingDialog.dismissWithAnimation()
                showSuccessMessage("Shipping Address Details Saved Successfully...")
                successDialog.setConfirmButton("Done",SweetAlertDialog.OnSweetClickListener {
                    val intent = Intent(applicationContext,Payment::class.java)
                    finish()
                    startActivity(intent)
                })
            }else{
                loadingDialog.dismissWithAnimation()
                try{
                    throw result.exception!!
                }catch(e: DatabaseException){
                    showErrorMessage(e.message.toString())
                }
                showErrorMessage("Something went wrong while saving data...")
            }
        }

    }
    private fun showLoadingMessage(message:String){
        loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText("Please Wait...")
            .setContentText(message)
        loadingDialog.setCancelable(false)
        loadingDialog.show()
    }
    private fun showErrorMessage(errorText:String){
        errorDialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        errorDialog.setCancelable(true)
        errorDialog.setTitleText("Error...!")
        errorDialog.setContentText(errorText)
        errorDialog.show()
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
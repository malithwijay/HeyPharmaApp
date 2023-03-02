package com.saclim.heypharmaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ForgetPassword : AppCompatActivity() {

    private lateinit var resetEmail:TextInputEditText
    private lateinit var textFpInputEmail:TextInputLayout
    private lateinit var btnResetCancel:MaterialButton
    private lateinit var btnReset:MaterialButton
    private lateinit var loadingDialog: SweetAlertDialog
    private lateinit var successDialog: SweetAlertDialog
    private lateinit var errorDialog: SweetAlertDialog
    private lateinit var confirmDialog: SweetAlertDialog
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        resetEmail = findViewById(R.id.ResetEmail)
        textFpInputEmail = findViewById(R.id.textFpInputEmail)
        btnReset = findViewById(R.id.btnReset)
        btnResetCancel = findViewById(R.id.btnResetCancel)

        textFpInputEmail.helperText=""

        btnResetCancel.setOnClickListener {
            val intent = Intent(this,LoginScreen::class.java)
            finish()
            startActivity(intent)
        }
        btnReset.setOnClickListener {
            if(resetEmail.text.isNullOrEmpty()){
                textFpInputEmail.helperText="*Enter Your Email"
            }else if(!("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex().matches(resetEmail.text.toString()))){
                textFpInputEmail.helperText="*Enter Valid Email"
            }else {
                showConfirmMessage("Are you sure to reset your password?")
                confirmDialog.setConfirmButton("Yes", SweetAlertDialog.OnSweetClickListener {
                    confirmDialog.dismissWithAnimation()
                    resetPassword()
                })
                confirmDialog.setCancelButton("No", SweetAlertDialog.OnSweetClickListener {
                    confirmDialog.dismissWithAnimation()
                })
            }
        }
    }
    private fun resetPassword(){
        showLoadingMessage("Resetting the Password Please wait...")
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.sendPasswordResetEmail(resetEmail.text.toString())
            .addOnCompleteListener { result->
                if(result.isSuccessful){
                    loadingDialog.dismissWithAnimation()
                    showSuccessMessage("Your will receive an email shortly...")
                    successDialog.setConfirmButton("OK",SweetAlertDialog.OnSweetClickListener {
                        successDialog.dismissWithAnimation()
                        finish()
                    })
                }else{
                    loadingDialog.dismissWithAnimation()
                    showErrorMessage(result.exception!!.message.toString())
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
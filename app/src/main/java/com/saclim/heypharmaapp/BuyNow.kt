package com.saclim.heypharmaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.math.RoundingMode
import java.text.DecimalFormat

class BuyNow : AppCompatActivity() {
    private lateinit var prescriptionID:String
    private lateinit var pharmacyID:String
    private var total:Double=0.0
    private var netTotal:Double=0.0
    private var presID:String=""
    private lateinit var loadingDialog: SweetAlertDialog
    private lateinit var errorDialog: SweetAlertDialog
    private lateinit var confirmDialog: SweetAlertDialog
    private lateinit var successDialog: SweetAlertDialog
    private lateinit var databaseReference: DatabaseReference
    private lateinit var txtPharmacyTp: TextView
    private lateinit var txtPharmacyName: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtCharges: TextView
    private lateinit var txtNet: TextView
    private lateinit var btnBuyNow: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_now)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(0).isChecked=false

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

        txtPharmacyName = findViewById(R.id.txtPharmacyName)
        txtPharmacyTp = findViewById(R.id.txtPharmacyTp)
        txtTotal = findViewById(R.id.txtTotal)
        txtCharges = findViewById(R.id.txtCharges)
        txtNet = findViewById(R.id.txtNet)
        btnBuyNow = findViewById(R.id.btnBuyNow)


        val extra = intent.extras
        if(extra!=null){
            prescriptionID=extra.getString("prescriptionID").toString()
            loadPrescriptionDetails()

        }else{
            loadingDialog.dismissWithAnimation()
            val intent = Intent(this,MyPrescription::class.java)
            finish()
            startActivity(intent)
        }
        btnBuyNow.setOnClickListener {
            showConfirmMessage("Proceed to payment details")
            confirmDialog.setConfirmButton("Ok",SweetAlertDialog.OnSweetClickListener {
                confirmDialog.dismissWithAnimation()
                val dataToSend = arrayOf<String>(presID,netTotal.toString())
                val intent = Intent(this,ShippingDetails::class.java)
                intent.putExtra("data",dataToSend)
                finish()
                startActivity(intent)
            })
            confirmDialog.setCancelButton("Cancel",SweetAlertDialog.OnSweetClickListener {
                confirmDialog.dismissWithAnimation()
            })
        }
    }

    private fun loadPrescriptionDetails(){
        showLoadingMessage("Loading Prescription Details...")
        databaseReference = FirebaseDatabase.getInstance().getReference("Prescription").child(prescriptionID)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                loadingDialog.dismissWithAnimation()
                val prescription = snapshot.getValue<Presciption>()
                if(prescription!=null){

                    pharmacyID=prescription.Phar_Id.toString()
                    total=prescription.Pres_price!!.toDouble()
                    presID = prescription.Prescription_id.toString()
                    loadPharmacyDetails()
                    calculateBillPrice()
                }else{
                    loadingDialog.dismissWithAnimation()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissWithAnimation()
            }
        })
    }
    private fun loadPharmacyDetails(){
        //showLoadingMessage("Loading Pharmacy Details...")
        databaseReference = FirebaseDatabase.getInstance().getReference("Pharmacy").child(pharmacyID)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //loadingDialog.dismissWithAnimation()
                val pharmacy = snapshot.getValue<Pharmacy>()
                if (pharmacy != null) {
                    txtPharmacyName.setText(pharmacy.Name)
                    txtPharmacyTp.setText(pharmacy.telephone)
                }else{
                    //loadingDialog.dismissWithAnimation()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //loadingDialog.dismissWithAnimation()
            }
        })
    }
    private fun calculateBillPrice(){
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        txtTotal.setText(df.format(total).toString())
        txtCharges.setText(df.format((total*0.2)).toString())
        netTotal = (total+total*0.2)
        txtNet.setText(df.format(netTotal).toString())
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
package com.saclim.heypharmaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.saclim.heypharmaapp.ShippingDetails
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.bottomNavigationView
import kotlinx.android.synthetic.main.activity_shipping_details.*

class ShippingDetails : AppCompatActivity() {
    private lateinit var btnProceed:MaterialButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private var presID=""
    private var billAmount=""
    private lateinit var txtAddressName:TextView
    private lateinit var txtAddressNo:TextView
    private lateinit var txtAddressLine1:TextView
    private lateinit var txtAddressLine2:TextView
    private lateinit var txtAddressCity:TextView
    private lateinit var txtAddressProvince:TextView
    private lateinit var loadingDialog: SweetAlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipping_details)

        btnProceed = findViewById(R.id.btnProceed)
        txtAddressName = findViewById(R.id.txtAddressName)
        txtAddressNo = findViewById(R.id.txtAddressNo)
        txtAddressLine1 = findViewById(R.id.txtAddressLine1)
        txtAddressLine2 = findViewById(R.id.txtAddressLine2)
        txtAddressCity = findViewById(R.id.txtAddressCity)
        txtAddressProvince = findViewById(R.id.txtAddressProvince)

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
                    val intent= Intent(this,DashboardHome::class.java)
                    finish()
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }

            }
            false
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        loadShippingDetails()

        val extra = intent.extras
        if(extra!=null){
            val data = extra.getStringArray("data")
            presID=data!![0]
            billAmount=data!![1]
        }

        btnProceed.setOnClickListener {
            val dataToSend = arrayOf<String>(presID,billAmount.toString())
            val intent = Intent(this,PaymentDetails::class.java)
            intent.putExtra("data",dataToSend)
            finish()
            startActivity(intent)
        }
    }
    private fun loadShippingDetails(){
        showLoadingMessage("Loading Shipping Details...")
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("ShippingAddress")
        databaseReference.child(firebaseAuth.currentUser!!.uid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val shipAddress = snapshot.getValue<ShippingAddress>()
                if(shipAddress!=null){
                    txtAddressName.text=shipAddress.fullName
                    txtAddressNo.text=shipAddress.shipTelephone
                    txtAddressLine1.text=shipAddress.addressLine1
                    txtAddressLine2.text=shipAddress.addressLine2
                    txtAddressCity.text=shipAddress.city
                    txtAddressProvince.text=shipAddress.province
                    loadingDialog.dismissWithAnimation()
                }else{
                    loadingDialog.dismissWithAnimation()
                    restartThis()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissWithAnimation()
            }
        })
    }
    private fun restartThis(){
        val dataToSend = arrayOf<String>(presID,billAmount.toString())
        val intent = Intent(this,ShippingDetails::class.java)
        intent.putExtra("data",dataToSend)
        finish()
        startActivity(intent)
    }
    private fun showLoadingMessage(message:String){
        loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText("Please Wait...")
            .setContentText(message)
        loadingDialog.setCancelable(false)
        loadingDialog.show()
    }
}
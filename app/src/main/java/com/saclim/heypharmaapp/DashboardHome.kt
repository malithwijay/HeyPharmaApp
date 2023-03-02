package com.saclim.heypharmaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.Pharmacy
import kotlinx.android.synthetic.main.activity_dashboard.bottomNavigationView
import kotlinx.android.synthetic.main.activity_pharmacy.*

class DashboardHome : AppCompatActivity() {

    private lateinit var findMed:CardView
    private lateinit var myPrescriptions:CardView
    private lateinit var getQuote:CardView
    private lateinit var pharmacy:CardView
    private lateinit var username:TextView
    private lateinit var loadingDialog: SweetAlertDialog
    private lateinit var errorDialog: SweetAlertDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findMed = findViewById(R.id.FindMed)
        myPrescriptions = findViewById(R.id.MyPrescriptions)
        getQuote = findViewById(R.id.GetQuote)
        pharmacy = findViewById(R.id.Pharmacy)
        username = findViewById(R.id.username)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(0).isChecked=true
        //bottomNavigationView.menu.getItem(2).isEnabled = false

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

                R.id.Home -> {
                    val intent= Intent(this, DashboardHome::class.java)
                    finish()
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }


            }
            false
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        findMed.setOnClickListener{
            val intent= Intent(this, ScanHere::class.java)
            finish()
            startActivity(intent)

        }

        myPrescriptions.setOnClickListener{
            val intent= Intent(this, MyPrescription::class.java)
            finish()
            startActivity(intent)

        }

        getQuote.setOnClickListener{
            val intent= Intent(this, BuyMedicine::class.java)
            finish()
            startActivity(intent)
        }

        pharmacy.setOnClickListener{
            val intent= Intent(this, ShowPharmacy::class.java)
            finish()
            startActivity(intent)
        }

        try {
            loadUserDetails()
        }catch (e:Exception) {
            showErrorMessage(e.message.toString())
        }
    }
    private fun loadUserDetails(){
        showLoadingMessage("Loading your details please wait...")
        val firebaseAuth = FirebaseAuth.getInstance()
        val databaseReference = FirebaseDatabase.getInstance().getReference("Member").child(firebaseAuth.currentUser!!.uid)
        databaseReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                loadingDialog.dismissWithAnimation()
                val memberDetails = snapshot.getValue<Member>()
                if(memberDetails!=null) {
                    username.text=memberDetails.name
                }else {
                    loadingDialog.dismissWithAnimation()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissWithAnimation()
            }
        })
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
    }
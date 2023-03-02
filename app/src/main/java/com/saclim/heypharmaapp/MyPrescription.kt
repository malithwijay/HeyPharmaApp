package com.saclim.heypharmaapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.bottomNavigationView
import kotlinx.android.synthetic.main.activity_my_prescription.*
import kotlinx.android.synthetic.main.my_prescriptions_recycler_item.*
import org.w3c.dom.Text
import java.io.File

class MyPrescription : AppCompatActivity() {
    
    private lateinit var databaseReference:DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var prescriptionList:ArrayList<Presciption>
    private lateinit var myPrescriptionRecyclePresciption:RecyclerView
    private lateinit var loadingDialog:SweetAlertDialog
    private lateinit var errorDialog:SweetAlertDialog
    private lateinit var confirmDialog:SweetAlertDialog
    private lateinit var successDialog:SweetAlertDialog
    private lateinit var pharmacyList:ArrayList<Pharmacy>
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseStorage: FirebaseStorage
    private var imageUri: Uri?=null
    private lateinit var shipAddressStatus:String
    private lateinit var btnMyPrescriptionCancel:MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_prescription)

        prescriptionList = arrayListOf<Presciption>()
        myPrescriptionRecyclePresciption = findViewById(R.id.MyPrescriptionRecycle)
        myPrescriptionRecyclePresciption.adapter=prescriptionAdapter()
        myPrescriptionRecyclePresciption.layoutManager = LinearLayoutManager(this)
        myPrescriptionRecyclePresciption.setHasFixedSize(true)
        pharmacyList = arrayListOf<Pharmacy>()
        shipAddressStatus=""
        btnMyPrescriptionCancel = findViewById(R.id.btnMyPrescriptionCancel)

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

        loadPrescriptionDetails()

        btnMyPrescriptionCancel.setOnClickListener {
            val intent = Intent(this,DashboardHome::class.java)
            finish()
            startActivity(intent)
        }
        
    }
    private fun loadPrescriptionDetails(){
        checkShippingAddress()
        showLoadingMessage("Loading Prescription Details...")

        firebaseAuth = FirebaseAuth.getInstance()
        val userID:String = firebaseAuth.currentUser!!.uid.toString()

        databaseReference = FirebaseDatabase.getInstance().getReference("Prescription")
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(prescriptionSnapshot in snapshot.children){
                        val prescription = prescriptionSnapshot.getValue(com.saclim.heypharmaapp.Presciption::class.java)
                        if(prescription==null){showErrorMessage("Prescription Null")}

                        if(prescription!!.Member_id==userID && prescription!!.Status!="Ordered"){
                            prescriptionList.add(prescription)
                        }
                    }
                    myPrescriptionRecyclePresciption.adapter?.notifyDataSetChanged()
                    loadingDialog.dismissWithAnimation()
                }else{
                    loadingDialog.dismissWithAnimation()
                    showErrorMessage("Prescription Details are not available...")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissWithAnimation()
                showErrorMessage("Data is not available go back and try again")
            }
        })

    }
    private inner class prescriptionAdapter : RecyclerView.Adapter<PrescriptionViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_prescriptions_recycler_item,
                parent,false)

            return PrescriptionViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: PrescriptionViewHolder, position: Int) {
            val currentItem = prescriptionList[position]

            databaseReference = FirebaseDatabase.getInstance().getReference("Pharmacy").child(currentItem.Phar_Id.toString())
            databaseReference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pharmacy = snapshot.getValue<Pharmacy>()
                    if (pharmacy != null) {
                        holder.txtRePrePhaName.text=pharmacy.Name
                        holder.txtRePreTp.text=pharmacy.telephone
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })

            holder.txtRePreId.text=currentItem.Prescription_id
            holder.txtRePreBuyerEmail.text = ""
            if(currentItem.Pres_price==null)
                holder.txtRePrePrice.text="0.00"
            else
                holder.txtRePrePrice.text=currentItem.Pres_price
            holder.txtRePreStatus.text=currentItem.Status

            storageReference = FirebaseStorage.getInstance().getReference(currentItem.Pres_Image.toString())

            val localFile:File = File.createTempFile("tempFile",".jpeg || .png")
            storageReference.getFile(localFile).addOnSuccessListener {
                val bitmap:Bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                Glide.with(applicationContext)
                    .load(bitmap)
                    .override(100,110)
                    .into(holder.imgPrescription)
            }
            if(currentItem.Status=="Pending") {
                holder.select_prescription_click.setBackgroundColor(Color.parseColor("#FC7D96"))
                holder.txtRePreId.setTextColor(Color.parseColor("#000000"))
            }else{
                holder.select_prescription_click.setBackgroundColor(Color.parseColor("#FFFFFF"))
                holder.txtRePreId.setTextColor(Color.parseColor("#A9A9A9"))
            }
            holder.select_prescription_click.setOnClickListener {
                val selectedItem = prescriptionList[position]
                if(selectedItem.Status!="Pending") {
                    if(!shipAddressStatus.isNullOrEmpty()) {
                        showConfirmMessage("Are you sure to buy prescription: ${selectedItem.Prescription_id}")
                        confirmDialog.setConfirmButton(
                            "Yes",
                            SweetAlertDialog.OnSweetClickListener {
                                startBuyNow(selectedItem.Prescription_id.toString())
                                confirmDialog.dismissWithAnimation()

                            })
                        confirmDialog.setCancelButton("No", SweetAlertDialog.OnSweetClickListener {
                            Toast.makeText(applicationContext, "Not Selected", Toast.LENGTH_SHORT)
                                .show()
                            confirmDialog.dismissWithAnimation()
                        })
                    }else{
                        showErrorMessage("Enter your shipping address to continue")
                    }
                }else{
                    showErrorMessage("This prescription still under process\nwe will get back to you soon...!")
                }
            }

            holder.select_prescription_click.setOnLongClickListener {
                val selectedItem = prescriptionList[position]
                this@MyPrescription.showConfirmMessage("Are you sure to delete prescription: ${selectedItem.Prescription_id}")
                    confirmDialog.setCustomImage(R.drawable.ic_baseline_priority_high_24)
                    confirmDialog.setConfirmButton("Yes", SweetAlertDialog.OnSweetClickListener {
                        databaseReference = FirebaseDatabase.getInstance().getReference("Prescription").child(selectedItem.Prescription_id.toString())
                        databaseReference.addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                snapshot.ref.removeValue()
                                showSuccessMessage("Prescription Deleted Successfully...!")
                                successDialog.setConfirmButton("OK",SweetAlertDialog.OnSweetClickListener {
                                    restartThis()
                                })
                            }
                            override fun onCancelled(error: DatabaseError) {
                                showErrorMessage(error.message)
                            }
                        })
                        confirmDialog.dismissWithAnimation()

                    })
                    confirmDialog.setCancelButton("No", SweetAlertDialog.OnSweetClickListener {
                        Toast.makeText(applicationContext, "Not Selected", Toast.LENGTH_SHORT).show()
                        confirmDialog.dismissWithAnimation()
                    })

                return@setOnLongClickListener true
            }

        }

        override fun getItemCount(): Int {
            return prescriptionList.size
        }

    }
    private inner class PrescriptionViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        val txtRePreId : TextView = itemView.findViewById(R.id.txtRePreId)
        val txtRePrePhaName : TextView = itemView.findViewById(R.id.txtRePrePhaName)
        val txtRePreTp : TextView = itemView.findViewById(R.id.txtRePreTp)
        val txtRePrePrice : TextView = itemView.findViewById(R.id.txtRePrePrice)
        val txtRePreStatus : TextView = itemView.findViewById(R.id.txtRePreStatus)
        val imgPrescription : ImageView = itemView.findViewById(R.id.imgPrescription)
        val txtRePreBuyerEmail : TextView = itemView.findViewById(R.id.txtRePreBuyerEmail)
        val select_prescription_click : LinearLayout = itemView.findViewById(R.id.select_prescription_click)

    }
    private fun checkShippingAddress() {
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("ShippingAddress").child(firebaseAuth.currentUser!!.uid)
        databaseReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val shipAdd = snapshot.getValue<ShippingAddress>()
                if(shipAdd!=null){
                    shipAddressStatus= shipAdd.fullName.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                shipAddressStatus=""
            }
        })
    }
    private fun startBuyNow(prescriptionID:String){
        val intent= Intent(this, BuyNow::class.java)
        finish()
        intent.putExtra("prescriptionID",prescriptionID)
        startActivity(intent)
    }
    private fun restartThis(){
        val intent= Intent(this, MyPrescription::class.java)
        finish()
        startActivity(intent)
    }
    private fun showLoadingMessage(message:String){
        loadingDialog = SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText("Please Wait...")
            .setContentText(message)
        loadingDialog.setCancelable(false)
        loadingDialog.show()
    }
    private fun showErrorMessage(errorText:String){
        errorDialog = SweetAlertDialog(this,SweetAlertDialog.ERROR_TYPE)
        errorDialog.setCancelable(true)
        errorDialog.setTitleText("Error...!")
        errorDialog.setContentText(errorText)
        errorDialog.show()
    }
    private fun showConfirmMessage(message:String){
        confirmDialog = SweetAlertDialog(this,SweetAlertDialog.CUSTOM_IMAGE_TYPE)
        confirmDialog.setCancelable(true)
        confirmDialog.setCustomImage(R.drawable.question_mark)
        confirmDialog.setTitleText("Confirm...!")
        confirmDialog.setContentText(message)
        confirmDialog.show()
    }
    private fun showSuccessMessage(message:String){
        successDialog = SweetAlertDialog(this,SweetAlertDialog.SUCCESS_TYPE)
        successDialog.setCancelable(true)
        successDialog.setTitleText("Done...!")
        successDialog.setContentText(message)
        successDialog.show()
    }
}
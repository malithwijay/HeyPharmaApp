package com.saclim.heypharmaapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_dashboard.*

class ShowPharmacy :AppCompatActivity() {
    private lateinit var pharmacyRecycle: RecyclerView
    private lateinit var pharmacyList: ArrayList<Pharmacy>
    private lateinit var searchPharmacyList: ArrayList<Pharmacy>
    private lateinit var tempPharmacyList: ArrayList<Pharmacy>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var loadingDialog: SweetAlertDialog
    private lateinit var errorDialog: SweetAlertDialog
    private lateinit var textSearchPharmacy: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pharmacy)

        textSearchPharmacy = findViewById(R.id.textSearchPharmacy)
        pharmacyList = ArrayList<Pharmacy>()
        tempPharmacyList = ArrayList<Pharmacy>()
        pharmacyRecycle = findViewById(R.id.MyPrescriptionRecycle)

        pharmacyRecycle.adapter=pharmacyAdapter()
        pharmacyRecycle.layoutManager = LinearLayoutManager(this)
        pharmacyRecycle.setHasFixedSize(true)



        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(2).isEnabled = false


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

        loadPharmacyDetails()

        textSearchPharmacy.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(p0.isNullOrEmpty()){
                    pharmacyList.clear()
                    pharmacyList.addAll(tempPharmacyList)
                    pharmacyRecycle.adapter?.notifyDataSetChanged()
                }else{
                    filter(p0.toString())
                }
            }
        })
    }


    private fun loadPharmacyDetails(){
        showLoadingMessage("Loading Pharmacy Details...")

        databaseReference = FirebaseDatabase.getInstance().getReference("Pharmacy")

        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(pharmacySnapshot in snapshot.children){
                        val pharmacy = pharmacySnapshot.getValue(com.saclim.heypharmaapp.Pharmacy::class.java)
                        if(pharmacy==null){showErrorMessage("Pharmacy Null")}
                        pharmacyList.add(pharmacy!!)

                    }
                    tempPharmacyList.addAll(pharmacyList)
                    pharmacyRecycle.adapter?.notifyDataSetChanged()
                    loadingDialog.dismissWithAnimation()
                }else{
                    loadingDialog.dismissWithAnimation()
                    showErrorMessage("Pharmacy Details are not available...")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissWithAnimation()
                showErrorMessage("Data is not available go back and try again")
            }
        })

    }
    private inner class pharmacyAdapter : RecyclerView.Adapter<PharmacyViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PharmacyViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.select_pharmacy_recycler_item,
                parent,false)

            return PharmacyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: PharmacyViewHolder, position: Int) {
            val currentItem = pharmacyList[position]

            holder.txtReSelectPhaName.text=currentItem.Name
            holder.txtReSelectPhaTp.text=currentItem.telephone
            holder.txtReSelectPhaLocation.text=currentItem.Location
            Glide.with(applicationContext)
                .load(currentItem.ImagePharmacy)
                .override(100, 70)
                .into(holder.imgSelectPharmacy)

            holder.select_pharmacy_recycle_click.setOnClickListener {
                val selectedItem = pharmacyList[position]
            }
        }

        override fun getItemCount(): Int {
            return pharmacyList.size
        }

    }
    private inner class PharmacyViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        val txtReSelectPhaName : TextView = itemView.findViewById(R.id.txtReSelectPhaName)
        val txtReSelectPhaTp : TextView = itemView.findViewById(R.id.txtReSelectPhaTp)
        val txtReSelectPhaLocation : TextView = itemView.findViewById(R.id.txtReSelectPhaLocation)
        val imgSelectPharmacy : ImageView = itemView.findViewById(R.id.imgSelectPharmacy)
        val select_pharmacy_recycle_click : LinearLayout = itemView.findViewById(R.id.select_pharmacy_recycle_click)
    }

    private fun filter(filterText:String){
        val filteredItem = ArrayList<Pharmacy>()

        for(item in pharmacyList){
            if(item.Name!!.toLowerCase().contains(filterText.toLowerCase())){
                filteredItem.add(item)
            }
        }
        pharmacyList.clear()
        pharmacyList.addAll(filteredItem)
        pharmacyRecycle.adapter?.notifyDataSetChanged()
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
}
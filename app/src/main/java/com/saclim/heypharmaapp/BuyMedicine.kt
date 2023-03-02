package com.saclim.heypharmaapp

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.util.*


class BuyMedicine : AppCompatActivity() {
    private lateinit var loadingDialog:SweetAlertDialog
    private lateinit var databaseReference:DatabaseReference
    private lateinit var myPrescriptionRecycle:RecyclerView
    private lateinit var pharmacyList:ArrayList<Pharmacy>
    private lateinit var errorDialog:SweetAlertDialog
    private lateinit var confirmDialog: SweetAlertDialog
    private lateinit var successDialog: SweetAlertDialog
    private lateinit var storageReference:StorageReference
    private lateinit var firebaseStorage:FirebaseStorage
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var pharmacyTelephone:String
    private lateinit var pharmacyName:String
    private lateinit var whatsAppMessage:String

    private companion object{
        private const val CAMERA_REQUEST_CODE=100
        private const val STORAGE_REQUEST_CODE=101
    }

    private var imageUri: Uri?=null

    private lateinit var cameraPermission:Array<String>
    private lateinit var storagePermission:Array<String>
    private lateinit var cameraCaptureImage:ImageView
    private lateinit var btnSelectPreCapture:MaterialButton
    private lateinit var btnMyPrescriptionGrtQuote:MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_medicine)

        cameraPermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        pharmacyName=""
        pharmacyTelephone=""
        whatsAppMessage=""
        myPrescriptionRecycle = findViewById(R.id.MyPrescriptionRecycle)
        cameraCaptureImage = findViewById(R.id.CameraCaptureImage)
        btnSelectPreCapture = findViewById(R.id.btnSelectPreCapture)
        btnMyPrescriptionGrtQuote = findViewById(R.id.btnMyPrescriptionGrtQuote)
        pharmacyList = arrayListOf<Pharmacy>()
        myPrescriptionRecycle.adapter=pharmacyAdapter()
        myPrescriptionRecycle.layoutManager = LinearLayoutManager(this)
        myPrescriptionRecycle.setHasFixedSize(true)
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

        btnSelectPreCapture.setOnClickListener {
            attachPrescription()
        }

        btnMyPrescriptionGrtQuote.setOnClickListener {
            if(imageUri!=null && pharmacyTelephone!=""){
                showConfirmMessage("Are you Sure to Place Order On ${pharmacyName}")
                confirmDialog.setConfirmButton("Yes",SweetAlertDialog.OnSweetClickListener {
                    savePrescriptionData()
                    confirmDialog.dismissWithAnimation()
                })
                confirmDialog.setCancelButton("No",SweetAlertDialog.OnSweetClickListener {
                    confirmDialog.dismissWithAnimation()
                    Toast.makeText(this,"Select Again...",Toast.LENGTH_SHORT).show()
                })

            }else{
                if(imageUri==null){
                    showErrorMessage("Please attach the prescription...")
                }
                else if(pharmacyTelephone==""){
                    showErrorMessage("Please select the Pharmacy...")
                }
            }

        }
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
                    myPrescriptionRecycle.adapter?.notifyDataSetChanged()
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
                showConfirmMessage("Are you sure to place order from ${selectedItem.Name}")
                confirmDialog.setConfirmButton("Yes",SweetAlertDialog.OnSweetClickListener {
                    pharmacyTelephone = selectedItem.telephone.toString()
                    pharmacyName = selectedItem.Name.toString()
                    confirmDialog.dismissWithAnimation()

                })
                confirmDialog.setCancelButton("No",SweetAlertDialog.OnSweetClickListener {
                    Toast.makeText(applicationContext,"Not Selected",Toast.LENGTH_LONG).show()
                    confirmDialog.dismissWithAnimation()
                })
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

    ////end of load pharmacy

    //// beginning of attach image file

    private fun attachPrescription(){
        val popupMenu = PopupMenu(this,btnSelectPreCapture)

        popupMenu.menu.add(Menu.NONE,1,1,"CAMERA")
        popupMenu.menu.add(Menu.NONE,2,2,"GALLERY")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->

            val id = menuItem.itemId
            if(id==1){
                if(checkCameraPermission()){
                    pickImageCamera()
                }
                else{
                    requstCameraPermission()
                }
            }
            else if(id==2){
                if(checkStoragePermission()){
                    pickImageGallery()
                }
                else{
                    requestStoragePermission()
                }
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                if(imageUri!=null){
                    Glide.with(applicationContext)
                        .load(imageUri)
                        .into(cameraCaptureImage)
                }else{
                    Toast.makeText(this,"Pick an Image...",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Operation Canceled...",Toast.LENGTH_SHORT).show()
            }
        }

    private fun pickImageCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Prescription")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Hey Pharma Prescription Image")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == Activity.RESULT_OK){
                Glide.with(applicationContext)
                    .load(imageUri)
                    .into(cameraCaptureImage)
            }
            else{
                Toast.makeText(this,"Operation Canceled...",Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkStoragePermission() : Boolean{
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission() : Boolean{
        val cameraResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        return cameraResult && storageResult
    }

    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,
            com.saclim.heypharmaapp.BuyMedicine.STORAGE_REQUEST_CODE
        )

    }

    private fun requstCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,
            com.saclim.heypharmaapp.BuyMedicine.CAMERA_REQUEST_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            com.saclim.heypharmaapp.BuyMedicine.CAMERA_REQUEST_CODE ->{
                if(grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if(cameraAccepted && storageAccepted){
                        pickImageCamera()
                    }else{
                        showErrorMessage("Camera & Storage Permission are Required")
                    }
                }
            }
            com.saclim.heypharmaapp.BuyMedicine.STORAGE_REQUEST_CODE ->{
                if(grantResults.isNotEmpty()){
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if(storageAccepted){
                        pickImageGallery()
                    }
                    else{
                        showErrorMessage("Storage Permission Are Required...!")
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////
    //end of selecting prescription/////////////////////////////
    ////////////////////////////////////////////////////////////
    /**
     *
     *
     *
     **/
    ////////////////////////////////////////////////////////////
    //beginning of save prescription/////////////////////////////
    ////////////////////////////////////////////////////////////

    private fun savePrescriptionData(){
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Prescription")

        val member_id:String = firebaseAuth.currentUser!!.uid.toString()
        val pres_image:String = "prescription/pres-${UUID.randomUUID().toString()}"
        val phar_id:String = pharmacyTelephone
        val prescriptionID = "HPP-${(0..9999999).shuffled().last()}"

        showLoadingMessage("Saving Prescription Details...")
        if(imageUri!=null){
            firebaseStorage = FirebaseStorage.getInstance()
            storageReference = firebaseStorage.getReference(pres_image)

            storageReference.putFile(imageUri!!).addOnCompleteListener { result->
                if(result.isSuccessful){
                    loadingDialog.dismissWithAnimation()

                    val presciptionData = Presciption(prescriptionID,member_id,pres_image,phar_id,"Pending")
                    databaseReference.child(prescriptionID).setValue(presciptionData).addOnCompleteListener { result->
                        if(result.isSuccessful){
                            loadingDialog.dismissWithAnimation()
                            showSuccessMessage("Prescription Saved...")
                            successDialog.setConfirmButton("OK",SweetAlertDialog.OnSweetClickListener {
                                val intent = Intent(this,DashboardHome::class.java)
                                successDialog.dismissWithAnimation()
                                finish()
                                startActivity(intent)
                            })
                        }else{
                            loadingDialog.dismissWithAnimation()
                            showErrorMessage("Something went wrong try again...")
                        }
                    }
                }else{
                    loadingDialog.dismissWithAnimation()
                    showErrorMessage("Something went wrong try again : \n Image Not Saved")
                }
            }
        }
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
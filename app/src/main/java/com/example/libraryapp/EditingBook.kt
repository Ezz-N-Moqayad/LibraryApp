package com.example.libraryapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class EditingBook : AppCompatActivity() {

    private lateinit var editNameBook: EditText
    private lateinit var editNameAuthor: EditText
    private lateinit var editLaunchYear: EditText
    private lateinit var editPrice: EditText
    private lateinit var editRatingBar: RatingBar
    private lateinit var editBookCover: ImageView
    private lateinit var editVideo: ImageView
    private lateinit var editBook: Button
    private lateinit var deleteBook: Button

    lateinit var database: DatabaseReference
    lateinit var progressDialog: ProgressDialog
    private lateinit var cameraPermissions: Array<String>
    private val VIDEO_PICK_GALLERY_CODE = 100
    private val VIDEO_PICK_CAMERA_CODE = 101
    private val CAMERA_REQUEST_CODE = 102
    private val PICK_IMAGE_REQUEST = 111
    private var videoUri: Uri? = null
    private var imageURI: Uri? = null
    private var flo = 0f
    private var edo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing_book)

        editNameBook = findViewById(R.id.editNameBook)
        editNameAuthor = findViewById(R.id.editNameAuthor)
        editLaunchYear = findViewById(R.id.editLaunchYear)
        editPrice = findViewById(R.id.editPrice)
        editBookCover = findViewById(R.id.editBookCover)
        editVideo = findViewById(R.id.editVideo)
        editRatingBar = findViewById(R.id.editRatingBar)
        editBook = findViewById(R.id.editBook)
        deleteBook = findViewById(R.id.deleteBook)

        database = Firebase.database.reference
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("Image Book")

        cameraPermissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        editNameBook.setText(intent.getStringExtra("Name_Book").toString())
        editNameAuthor.setText(intent.getStringExtra("Name_Author").toString())
        editLaunchYear.setText(intent.getStringExtra("Launch_Year").toString())
        editPrice.setText(intent.getStringExtra("Price_Book").toString())
        editRatingBar.rating = intent.getFloatExtra("Book_Review", 0f)
        flo = intent.getFloatExtra("Book_Review", 0f)

        editLaunchYear.setOnClickListener {
            val currentDate = Calendar.getInstance()
            val day = currentDate.get(Calendar.DAY_OF_MONTH)
            val month = currentDate.get(Calendar.MONTH)
            val year = currentDate.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this, { _, y, m, d ->
                    editLaunchYear.setText("$y / ${m + 1} / $d")
                }, year, month, day
            )
            picker.show()
        }

        editBookCover.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            editBookCover.setBackgroundResource(0)
            edo = 1
        }

        editVideo.setOnClickListener {
            videoPickDialog()
        }

        editRatingBar.setOnRatingBarChangeListener { _, fl, _ ->
            flo = fl
        }

        deleteBook.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Book")
            builder.setMessage("Do you want to Delete the Book?")
            builder.setPositiveButton("Yes") { _, _ ->
                deleteBook()
                Toast.makeText(this, "Delete Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ViewBooks::class.java))
            }
            builder.setNegativeButton("No") { d, _ ->
                d.dismiss()
            }
            builder.create().show()
        }

        editBook.setOnClickListener {
            if (editNameBook.text.isEmpty() || editNameAuthor.text.isEmpty() || editLaunchYear.text.isEmpty() || editPrice.text.isEmpty()) {
                Toast.makeText(this, "Fill Fields", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Edit Book")
                builder.setMessage("Do you want to Edit the Book?")
                builder.setPositiveButton("Yes") { _, _ ->
                    showDialog("Edit Book...")
                    val bitmap = (editBookCover.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val data = baos.toByteArray()
                    val childRef =
                        imageRef.child(System.currentTimeMillis().toString() + ".png")
                    val uploadTask = childRef.putBytes(data)
                    uploadTask.addOnFailureListener {
                        hideDialog()
                    }.addOnSuccessListener {
                        childRef.downloadUrl.addOnSuccessListener { uri ->
                            uploadVideoFirebase(uri.toString())
                        }
                    }
                }
                builder.setNegativeButton("No") { d, _ ->
                    d.dismiss()
                }
                builder.create().show()
            }
        }
    }

    private fun uploadVideoFirebase(uri: String) {
        val idVideo = System.currentTimeMillis()
        val filePathAndName = "Videos/video_$idVideo"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(videoUri!!).addOnSuccessListener { taskSnapshot ->
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val downloadUri = uriTask.result
            if (uriTask.isSuccessful) {
                editBook(edo, uri, downloadUri.toString())
                hideDialog()
                startActivity(Intent(this, ViewBooks::class.java))
                Toast.makeText(this, "Added Successfully", Toast.LENGTH_SHORT).show()
            } else {
                hideDialog()
                startActivity(Intent(this, ViewBooks::class.java))
                Toast.makeText(this, "Add Failed", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            hideDialog()
            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editBook(edo: Int, Image_Book: String, Uri_Video: String) {
        val values = mapOf(
            "Name_Book" to editNameBook.text.toString(),
            "Name_Author" to editNameAuthor.text.toString(),
            "Launch_Year" to editLaunchYear.text.toString(),
            "Price_Book" to editPrice.text.toString(),
            "Book_Review" to flo.toString()
        )
        val id = intent.getStringExtra("id").toString()
        database.child("Books").child(id).updateChildren(values).addOnSuccessListener {
            FCMService.sendRemoteNotification(
                "Edit Book",
                "${editNameBook.text} Book was edited recently"
            )
            finish()
        }.addOnFailureListener {
            Log.e("test", "onResume: ${it.message}")
        }
    }

    private fun deleteBook() {
        val id = intent.getStringExtra("id").toString()
        database.child("Books").child(id).removeValue()
    }

    private fun videoPickDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Pick Video From")
            .setItems(options) { _, i ->
                if (i == 0) {
                    if (!checkCameraPermissions()) {
                        requestCameraPermissions()
                    } else {
                        videoPickCamera()
                    }
                } else {
                    videoPickGallery()
                }
            }
            .show()
    }

    private fun requestCameraPermissions() {
        ActivityCompat.requestPermissions(
            this,
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun checkCameraPermissions(): Boolean {
        val result1 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val result2 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        return result1 && result2
    }

    private fun videoPickGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(intent, "Choose video"),
            VIDEO_PICK_GALLERY_CODE
        )
    }

    private fun videoPickCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE ->
                if (grantResults.isNotEmpty()) {
                    val cameraAccepted =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted =
                        grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted) {
                        videoPickCamera()
                    } else {
                        Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_PICK_CAMERA_CODE) {
                videoUri = data!!.data
            } else if (requestCode == VIDEO_PICK_GALLERY_CODE) {
                videoUri = data!!.data
            }
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDialog(text: String) {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage(text)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun hideDialog() {
        if (progressDialog.isShowing)
            progressDialog.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.Back -> startActivity(Intent(this, ViewBooks::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}

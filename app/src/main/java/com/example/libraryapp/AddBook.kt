package com.example.libraryapp

import android.app.Activity
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

class AddBook : AppCompatActivity() {

    private lateinit var addNameBook: EditText
    private lateinit var addNameAuthor: EditText
    private lateinit var addLaunchYear: EditText
    private lateinit var addPrice: EditText
    private lateinit var uploadBookCover: ImageView
    private lateinit var uploadVideo: ImageView
    private lateinit var addRatingBar: RatingBar
    private lateinit var addBook: Button

    lateinit var database: DatabaseReference
    lateinit var progressDialog: ProgressDialog
    private lateinit var cameraPermissions: Array<String>
    private val VIDEO_PICK_GALLERY_CODE = 100
    private val VIDEO_PICK_CAMERA_CODE = 101
    private val CAMERA_REQUEST_CODE = 102
    private val Pick_IMAGE_REQUEST = 111
    private var videoUri: Uri? = null
    private var imageURI: Uri? = null
    private var flo = 0f
    private var edo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        addNameBook = findViewById(R.id.addNameBook)
        addNameAuthor = findViewById(R.id.addNameAuthor)
        addLaunchYear = findViewById(R.id.addLaunchYear)
        addPrice = findViewById(R.id.addPrice)
        uploadBookCover = findViewById(R.id.uploadBookCover)
        uploadVideo = findViewById(R.id.uploadVideo)
        addRatingBar = findViewById(R.id.addRatingBar)
        addBook = findViewById(R.id.addBook)

        database = Firebase.database.reference
        val idRT = System.currentTimeMillis()
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("Image Book")

        cameraPermissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        addLaunchYear.setOnClickListener {
            val currentDate = Calendar.getInstance()
            val day = currentDate.get(Calendar.DAY_OF_MONTH)
            val month = currentDate.get(Calendar.MONTH)
            val year = currentDate.get(Calendar.YEAR)
            val picker = DatePickerDialog(
                this, { _, y, m, d ->
                    addLaunchYear.setText("$y / ${m + 1} / $d")
                }, year, month, day
            )
            picker.show()
        }

        uploadBookCover.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK
            intent.type = "image/*"
            startActivityForResult(intent, Pick_IMAGE_REQUEST)
            uploadBookCover.setBackgroundResource(0)
            edo = 1
        }

        uploadVideo.setOnClickListener {
            videoPickDialog()
        }

        addRatingBar.setOnRatingBarChangeListener { _, fl, _ ->
            flo = fl
        }

        addBook.setOnClickListener {
            if (addNameBook.text.isEmpty() || addNameAuthor.text.isEmpty()
                || addLaunchYear.text.isEmpty() || addPrice.text.isEmpty() || edo == 0
            ) {
                Toast.makeText(this, "Fill Fields", Toast.LENGTH_SHORT).show()
            } else if (videoUri == null) {
                Toast.makeText(this, "Pick the Video First", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Add Book")
                builder.setMessage("Do you want to Add the Book?")
                builder.setPositiveButton("Yes") { _, _ ->
                    showDialog("Uploading Book...")
                    val bitmap = (uploadBookCover.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val data = baos.toByteArray()
                    val childRef = imageRef.child(System.currentTimeMillis().toString() + ".png")
                    val uploadTask = childRef.putBytes(data)
                    uploadTask.addOnFailureListener {
                        hideDialog()
                    }.addOnSuccessListener {
                        childRef.downloadUrl.addOnSuccessListener { uri ->
                            uploadVideoFirebase(idRT.toString(), uri.toString())
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

    private fun uploadVideoFirebase(idRT: String, uri: String) {
        val idVideo = System.currentTimeMillis()
        val filePathAndName = "Videos/video_$idVideo"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(videoUri!!).addOnSuccessListener { taskSnapshot ->
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val downloadUri = uriTask.result
            if (uriTask.isSuccessful) {
                addBook(
                    idRT,
                    addNameBook.text.toString(),
                    addNameAuthor.text.toString(),
                    addLaunchYear.text.toString(),
                    addPrice.text.toString(),
                    downloadUri.toString(),
                    uri,
                    flo.toString()
                )
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

    private fun addBook(
        id: String,
        nameBook: String,
        nameAuthor: String,
        launchYear: String,
        price: String,
        Uri_Video: String,
        image: String,
        bookReview: String
    ) {
        val book = hashMapOf(
            "id" to id,
            "Name_Book" to nameBook,
            "Name_Author" to nameAuthor,
            "Launch_Year" to launchYear,
            "Price_Book" to price,
            "Uri_Video" to Uri_Video,
            "Image_Book" to image,
            "Book_Review" to bookReview
        )
        database.child("Books/$id").setValue(book)
            .addOnSuccessListener {
                FCMService.sendRemoteNotification("Add Book", "$nameBook Book was added recently")
                finish()
            }.addOnFailureListener {
                Log.e("test", "onResume: ${it.message}")
            }
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
        if (requestCode == Pick_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageURI = data!!.data
            uploadBookCover.setImageURI(imageURI)
        }
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

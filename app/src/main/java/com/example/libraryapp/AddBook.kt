package com.example.libraryapp

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

class AddBook : AppCompatActivity() {

    private lateinit var addNameBook: EditText
    private lateinit var addNameAuthor: EditText
    private lateinit var addLaunchYear: EditText
    private lateinit var addPrice: EditText
    private lateinit var uploadBookCover: ImageView
    private lateinit var addRatingBar: RatingBar
    private lateinit var addBook: Button

    lateinit var database: DatabaseReference;
    lateinit var progressDialog: ProgressDialog


    private val Pick_IMAGE_REQUEST = 111
    var imageURI: Uri? = null
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
        addRatingBar = findViewById(R.id.addRatingBar)
        addBook = findViewById(R.id.addBook)

        database = Firebase.database.reference
        val id = System.currentTimeMillis()
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("Image Book")

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

        addRatingBar.setOnRatingBarChangeListener { _, fl, _ ->
            flo = fl
        }

        addBook.setOnClickListener {
            if (addNameBook.text.isEmpty() || addNameAuthor.text.isEmpty()
                || addLaunchYear.text.isEmpty() || addPrice.text.isEmpty() || edo == 0
            ) {
                Toast.makeText(this, "Fill Fields", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Add Book")
                builder.setMessage("Do you want to Add the Book?")
                builder.setPositiveButton("Yes") { _, _ ->

                    showDialog()
                    val bitmap = (uploadBookCover.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val data = baos.toByteArray()
                    val childRef = imageRef.child(System.currentTimeMillis().toString() + ".png")
                    val uploadTask = childRef.putBytes(data)
                    uploadTask.addOnFailureListener { exception ->
                        hideDialog()
                    }.addOnSuccessListener {
                        childRef.downloadUrl.addOnSuccessListener { uri ->
                            addBook(
                                id.toString(),
                                addNameBook.text.toString(),
                                addNameAuthor.text.toString(),
                                addLaunchYear.text.toString(),
                                addPrice.text.toString(),
                                uri.toString(),
                                flo.toString()
                            )
                            hideDialog()
                            Toast.makeText(this, "Added Successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ViewBooks::class.java))
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

    private fun addBook(
        id: String,
        nameBook: String,
        nameAuthor: String,
        launchYear: String,
        price: String,
        image: String,
        bookReview: String
    ) {
        val book = hashMapOf(
            "id" to id,
            "Name_Book" to nameBook,
            "Name_Author" to nameAuthor,
            "Launch_Year" to launchYear,
            "Price_Book" to price,
            "Image_Book" to image,
            "Book_Review" to bookReview
        )
        database.child("Books/$id").setValue(book)
    }

    private fun showDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Uploading ...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    private fun hideDialog() {
        if (progressDialog!!.isShowing)
            progressDialog!!.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Pick_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageURI = data!!.data
            uploadBookCover.setImageURI(imageURI)
        }
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

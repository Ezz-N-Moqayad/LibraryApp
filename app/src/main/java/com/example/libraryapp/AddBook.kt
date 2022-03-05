package com.example.libraryapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_book.*
import java.util.*

class AddBook : AppCompatActivity() {

    private var db: FirebaseFirestore? = null
    private var flo = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        db = Firebase.firestore
        val id = System.currentTimeMillis()

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

        addRatingBar.setOnRatingBarChangeListener { _, fl, _ ->
            flo = fl
        }

        addBook.setOnClickListener {
            if (addNameBook.text.isEmpty() || addNameAuthor.text.isEmpty() || addLaunchYear.text.isEmpty() || addPrice.text.isEmpty()) {
                Toast.makeText(this, "Fill Fields", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Add Book")
                builder.setMessage("Do you want to Add the Book?")
                builder.setPositiveButton("Yes") { _, _ ->
                    addBook(
                        id.toString(),
                        addNameBook.text.toString(),
                        addNameAuthor.text.toString(),
                        addLaunchYear.text.toString(),
                        addPrice.text.toString(),
                        flo.toString()
                    )
                    Toast.makeText(this, "Added Successfully", Toast.LENGTH_SHORT).show()
                    addNameBook.text.clear()
                    addNameAuthor.text.clear()
                    addLaunchYear.text.clear()
                    addPrice.text.clear()
                    startActivity(Intent(this, ViewBooks::class.java))
                }
                builder.setNegativeButton("No") { d, _ ->
                    startActivity(Intent(this, ViewBooks::class.java))
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
        bookReview: String
    ) {
        val book = hashMapOf(
            "id" to id,
            "Name_Book" to nameBook,
            "Name_Author" to nameAuthor,
            "Launch_Year" to launchYear,
            "Price" to price,
            "Book_Review" to bookReview
        )
        db!!.collection("Books").add(book)
    }
}

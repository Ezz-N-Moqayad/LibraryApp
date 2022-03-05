package com.example.libraryapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_editing_book.*
import java.util.*

class EditingBook : AppCompatActivity() {

    private var db: FirebaseFirestore? = null
    private var flo = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing_book)

        db = Firebase.firestore

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

        editRatingBar.setOnRatingBarChangeListener { _, fl, _ ->
            flo = fl
        }

        editNameBook.setText(intent.getStringExtra("Name_Book").toString())
        editNameAuthor.setText(intent.getStringExtra("Name_Author").toString())
        editLaunchYear.setText(intent.getStringExtra("Launch_Year").toString())
        editPrice.setText(intent.getStringExtra("Price").toString())
        editRatingBar.rating = intent.getFloatExtra("Book_Review", 0f)

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
                    editBook()
                    Toast.makeText(this, "Edit Successfully", Toast.LENGTH_SHORT).show()
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

    private fun editBook() {
        db!!.collection("Books")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.toObject<Book>()
                    if (document.get("id") == intent.getStringExtra("id")) {
                        db!!.collection("Books").document(document.id)
                            .update("Name_Book", editNameBook.text.toString())
                        db!!.collection("Books").document(document.id)
                            .update("Name_Author", editNameAuthor.text.toString())
                        db!!.collection("Books").document(document.id)
                            .update("Launch_Year", editLaunchYear.text.toString())
                        db!!.collection("Books").document(document.id)
                            .update("Price", editPrice.text.toString())
                        db!!.collection("Books").document(document.id)
                            .update("Book_Review", flo.toString())
                    }
                }
            }
    }

    private fun deleteBook() {
        db!!.collection("Books")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.toObject<Book>()
                    if (document.get("id") == intent.getStringExtra("id")) {
                        db!!.collection("Books").document(document.id).delete()
                    }
                }
            }
    }
}

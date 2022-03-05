package com.example.libraryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.book_item.view.*
import kotlinx.android.synthetic.main.activity_view_books.*

class ViewBooks : AppCompatActivity() {

    private var db: FirebaseFirestore? = null
    private var adapter: FirestoreRecyclerAdapter<Book, BookViewHolder>? = null
    var count = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_books)

        db = Firebase.firestore
        getAllBook()

        add_Books.setOnClickListener {
            startActivity(Intent(this, AddBook::class.java))
        }
    }

    private fun getAllBook() {

        val query = db!!.collection("Books")
        val options =
            FirestoreRecyclerOptions.Builder<Book>().setQuery(query, Book::class.java).build()

        adapter = object : FirestoreRecyclerAdapter<Book, BookViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
                val view =
                    LayoutInflater.from(this@ViewBooks).inflate(R.layout.book_item, parent, false)
                return BookViewHolder(view)
            }

            override fun onBindViewHolder(holder: BookViewHolder, position: Int, model: Book) {
                holder.numberBook.text = count.toString()
                holder.bookName.text = model.Name_Book
                holder.bookAuthor.text = model.Name_Author
                holder.launchYear.text = model.Launch_Year
                holder.bookReview.text = model.Book_Review
                holder.bookPrice.text = "$ ${model.Price}"
                holder.editBook.setOnClickListener {
                    intent(
                        model.id,
                        model.Name_Book,
                        model.Name_Author,
                        model.Launch_Year,
                        model.Book_Review.toFloat(),
                        model.Price
                    )
                }
                count++
            }
        }
        rvBook.layoutManager = LinearLayoutManager(this)
        rvBook.adapter = adapter
    }

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var numberBook = view.number_book!!
        var bookName = view.book_name!!
        var bookAuthor = view.book_author!!
        var launchYear = view.launch_year!!
        var bookReview = view.book_review!!
        var bookPrice = view.book_price!!
        val editBook = itemView.editBtn!!
    }

    fun intent(
        id: String,
        Name_Book: String,
        Name_Author: String,
        Launch_Year: String,
        Book_Review: Float,
        Price: String
    ) {
        val i = Intent(this, EditingBook::class.java)
        i.putExtra("id", id)
        i.putExtra("Name_Book", Name_Book)
        i.putExtra("Name_Author", Name_Author)
        i.putExtra("Launch_Year", Launch_Year)
        i.putExtra("Book_Review", Book_Review)
        i.putExtra("Price", Price)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }
}
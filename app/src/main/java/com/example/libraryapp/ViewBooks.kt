package com.example.libraryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ViewBooks : AppCompatActivity() {

    private lateinit var addBooks: FloatingActionButton
    private lateinit var rcvBook: RecyclerView

    lateinit var database: DatabaseReference;
    private var adapter: FirebaseRecyclerAdapter<Book, BookViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_books)

        addBooks = findViewById(R.id.add_Books)

        database = Firebase.database.reference
        getAllBook()

        addBooks.setOnClickListener {
            startActivity(Intent(this, AddBook::class.java))
        }
    }

    private fun getAllBook() {
        rcvBook = findViewById(R.id.rvBook)

        val query = database.child("Books")
        val options =
            FirebaseRecyclerOptions.Builder<Book>().setQuery(query, Book::class.java).build()

        adapter = object : FirebaseRecyclerAdapter<Book, BookViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
                val view =
                    LayoutInflater.from(this@ViewBooks).inflate(R.layout.book_item, parent, false)
                return BookViewHolder(view)
            }

            override fun onBindViewHolder(holder: BookViewHolder, position: Int, model: Book) {
                holder.bookName.text = model.Name_Book
                holder.bookAuthor.text = model.Name_Author
                holder.launchYear.text = model.Launch_Year
                holder.bookReview.text = model.Book_Review
                holder.bookPrice.text = model.Price_Book
                Glide.with(this@ViewBooks).load(model.Image_Book).into(holder.imageBook)
                holder.editBook.setOnClickListener {
                    intent(
                        model.id,
                        model.Name_Book,
                        model.Name_Author,
                        model.Launch_Year,
                        model.Image_Book,
                        model.Uri_Video,
                        model.Book_Review.toFloat(),
                        model.Price_Book
                    )
                }
                holder.viewVideoBtn.setOnClickListener {
                    val i = Intent(this@ViewBooks, VideoActivity::class.java)
                    i.putExtra("Name_Video", model.Name_Book)
                    i.putExtra("Uri_Video", model.Uri_Video)
                    startActivity(i)
                }
            }
        }
        rcvBook.layoutManager = LinearLayoutManager(this)
        rcvBook.adapter = adapter
    }

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageBook = view.findViewById<ImageView>(R.id.image_book)!!
        var bookName = view.findViewById<TextView>(R.id.book_name)!!
        var bookAuthor = view.findViewById<TextView>(R.id.book_author)!!
        var launchYear = view.findViewById<TextView>(R.id.launch_year)!!
        var bookReview = view.findViewById<TextView>(R.id.book_review)!!
        var bookPrice = view.findViewById<TextView>(R.id.book_price)!!
        val editBook = view.findViewById<Button>(R.id.editBtn)!!
        val viewVideoBtn = view.findViewById<Button>(R.id.viewVideoBtn)!!
    }

    fun intent(
        id: String,
        Name_Book: String,
        Name_Author: String,
        Launch_Year: String,
        Image_Book: String,
        Uri_Video: String,
        Book_Review: Float,
        Price_Book: String
    ) {
        val i = Intent(this, EditingBook::class.java)
        i.putExtra("id", id)
        i.putExtra("Name_Book", Name_Book)
        i.putExtra("Name_Author", Name_Author)
        i.putExtra("Launch_Year", Launch_Year)
        i.putExtra("Image_Book", Image_Book)
        i.putExtra("Uri_Video", Uri_Video)
        i.putExtra("Book_Review", Book_Review)
        i.putExtra("Price_Book", Price_Book)
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
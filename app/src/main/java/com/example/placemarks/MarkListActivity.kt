package com.example.placemarks

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class MarkListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlacedMarkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_mark_list)

        Timber.d("MarkListActivity opened")

        recyclerView =
            findViewById(R.id.marksRecyclerView)

        adapter = PlacedMarkAdapter(
            marks = AppData.placedMarks.findAll(),

            listener = object : OnMarkListener {

                override fun onMarkClick(mark: PlacedMark) {
                    editMark(mark)
                }

                override fun onMarkLongClick(mark: PlacedMark): Boolean {
                    confirmDelete(mark)
                    return true
                }
            }
        )

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = adapter

        val returnButton =
            findViewById<Button>(R.id.returnButton)

        returnButton.setOnClickListener {
            Timber.d("Returning to main menu")
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {

            adapter.updateMarks(
                AppData.placedMarks.findAll()
            )
        }
    }

    private fun editMark(mark: PlacedMark) {

        Timber.d("Editing mark id=${mark.id}")

        val intent =
            Intent(this, AddEditActivity::class.java)

        intent.putExtra("id", mark.id)

        startActivity(intent)
    }

    private fun confirmDelete(mark: PlacedMark) {

        AlertDialog.Builder(this)
            .setTitle("Delete this mark?")
            .setMessage("Are you sure you want to delete this mark?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMark(mark)
            }
            .setNegativeButton("Cancel") { _, _ ->
                Timber.d("Deletion cancelled")
            }
            .show()
    }

    private fun deleteMark(mark: PlacedMark) {

        if (AppData.placedMarks.delete(mark.id)) {
            Timber.d("Deleted mark id=${mark.id}")
        } else {
            Timber.w("Failed to delete mark id=${mark.id}")
        }

        adapter.updateMarks(
            AppData.placedMarks.findAll()
        )
    }
}
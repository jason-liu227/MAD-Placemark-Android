package com.example.placemarks

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts

class AddEditActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText
    private lateinit var imagePreview: ImageView
    private var selectedImageUri: Uri? = null

    private var editingId: Long? = null

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->

        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                Timber.w("Persistent image access unavailable")
            }

            selectedImageUri = uri
            imagePreview.setImageURI(uri)

            Timber.d("Image selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_edit)

        Timber.d("AddEditActivity opened")

        titleInput = findViewById(R.id.titleInput)

        descriptionInput = findViewById(R.id.descriptionInput)

        xInput = findViewById(R.id.xInput)

        yInput = findViewById(R.id.yInput)

        imagePreview = findViewById(R.id.imageView)

        val saveButton = findViewById<Button>(R.id.saveButton)

        val cancelButton = findViewById<Button>(R.id.cancelButton)

        editingId = intent.getLongExtra("id", -1L).takeIf { it != -1L }

        if (editingId != null) {

            Timber.d("Editing mark id=$editingId")

            findViewById<TextView>(R.id.formTitle).text = "Edit Mark"

            loadExistingMark(editingId!!)

        } else {
            Timber.d("Adding a new mark")
        }

        saveButton.setOnClickListener {
            Timber.d("Save button clicked")
            saveMark()
        }

        cancelButton.setOnClickListener {
            Timber.d("Add/Edit cancelled")
            finish()
        }

        findViewById<Button>(R.id.addImage)
            .setOnClickListener {
                imagePicker.launch(arrayOf("image/*"))
            }
    }

    private fun loadExistingMark(id: Long) {

        Timber.d("Loading mark id=$id")

        val mark = AppData.placedMarks.findOne(id)

        if (mark == null) {

            Timber.w("Mark not found: id=$id")

            Toast.makeText(
                this, "Mark not found", Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        titleInput.setText(mark.title)
        descriptionInput.setText(mark.description)
        xInput.setText(mark.x.toString())
        yInput.setText(mark.y.toString())
        selectedImageUri = mark.imageUri?.let { Uri.parse(it) }

        if (selectedImageUri != null) {
            imagePreview.setImageURI(selectedImageUri)
        } else {
            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun saveMark() {

        val title = titleInput.text.toString().trim()

        val description = descriptionInput.text.toString().trim()

        if (title.isBlank()) {
            Timber.w("Validation failed: title is empty")
            titleInput.error = "Title is required"
            return
        }

        val x = xInput.text.toString().toDoubleOrNull()

        if (x == null || !x.isFinite() || x !in -90.0..90.0) {
            Timber.w("Validation failed: invalid latitude")
            xInput.error = "Latitude must be between -90 and 90"
            return
        }

        val y = yInput.text.toString().toDoubleOrNull()

        if (y == null || !y.isFinite() || y !in -180.0..180.0) {
            Timber.w("Validation failed: invalid longitude")
            yInput.error = "Longitude must be between -180 and 180"
            return
        }

        try {

            if (editingId == null) {

                val mark = PlacedMark(
                    title = title,
                    description = description,
                    x = x,
                    y = y,
                    imageUri = selectedImageUri?.toString()
                )

                mark.validate()

                AppData.placedMarks.create(mark)

                Timber.d("Created mark id=${mark.id}")

                Toast.makeText(
                    this, "Mark created", Toast.LENGTH_SHORT
                ).show()

            } else {

                val mark = PlacedMark(
                    id = editingId!!,
                    title = title,
                    description = description,
                    x = x,
                    y = y,
                    imageUri = selectedImageUri?.toString()
                )

                mark.validate()

                if (!AppData.placedMarks.update(mark)) {
                    Toast.makeText(
                        this, "Mark not found", Toast.LENGTH_SHORT
                    ).show()

                    return
                }

                Timber.d("Updated mark id=${mark.id}")

                Toast.makeText(
                    this, "Mark updated", Toast.LENGTH_SHORT
                ).show()
            }

            finish()

        } catch (e: IllegalArgumentException) {

            Timber.w("Placemark validation failed")

            Toast.makeText(
                this, e.message ?: "Please check the entered values.", Toast.LENGTH_LONG
            ).show()
        }
        finish()
    }
}
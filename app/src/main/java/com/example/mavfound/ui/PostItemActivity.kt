package com.example.mavfound.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import com.example.mavfound.R
import com.example.mavfound.database.DatabaseHelper
import com.example.mavfound.models.Listing
import com.example.mavfound.utils.ThemeManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PostItemActivity : AppCompatActivity() {

    private lateinit var ivItemPreview: ImageView
    private lateinit var dbHelper: DatabaseHelper
    private var imageUri: Uri? = null
    private var photoPath: String? = null

    // Camera Launcher
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            ivItemPreview.setImageURI(imageUri)
            ivItemPreview.alpha = 1.0f
            Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery Launcher
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val copiedPath = copyUriToInternalStorage(uri)
            if (copiedPath != null) {
                photoPath = copiedPath
                ivItemPreview.setImageURI(Uri.fromFile(File(photoPath!!)))
                ivItemPreview.alpha = 1.0f
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_item)

        // 1. Initialize the animated background
        val rootLayout = findViewById<CoordinatorLayout>(R.id.postItemRootLayout)
        val animationDrawable = rootLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()

        dbHelper = DatabaseHelper(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        ivItemPreview = findViewById(R.id.ivItemPreview)
        val btnCamera = findViewById<MaterialButton>(R.id.btnCapturePhoto)
        val btnGallery = findViewById<MaterialButton>(R.id.btnChooseGallery)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnSubmitListing)

        btnCamera.setOnClickListener { launchCamera() }
        btnGallery.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnSubmit.setOnClickListener {
            saveListing()
        }
    }

    private fun launchCamera() {
        val photoFile = createImageFile()
        photoFile?.also {
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
            imageUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
                photoPath = absolutePath
            }
        } catch (e: Exception) { null }
    }

    private fun copyUriToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("GALLERY_${timeStamp}_", ".jpg", storageDir)

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveListing() {
        val title = findViewById<TextInputEditText>(R.id.etTitle).text.toString().trim()
        val location = findViewById<TextInputEditText>(R.id.etLocation).text.toString().trim()
        val desc = findViewById<TextInputEditText>(R.id.etDescription).text.toString().trim()
        val rewardStr = findViewById<TextInputEditText>(R.id.etReward).text.toString().trim()
        val question = findViewById<TextInputEditText>(R.id.etSecurityQuestion).text.toString().trim()
        val answer = findViewById<TextInputEditText>(R.id.etSecurityAnswer).text.toString().trim()

        // Validation
        if (title.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in title and location", Toast.LENGTH_SHORT).show()
            return
        }

        // Get Current User ID from SharedPreferences
        val sharedPrefs = getSharedPreferences("MavFoundPrefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPrefs.getInt("CURRENT_USER_ID", -1)

        // LOGGING FOR DEBUGGING: Check your Logcat to see if this is -1
        Log.d("PostItem", "Attempting to save listing for User ID: $currentUserId")

        if (currentUserId == -1) {
            Toast.makeText(this, "User session error. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        val newListing = Listing(
            listingId = 0, // DB will auto-increment
            listerId = currentUserId,
            title = title,
            description = desc,
            location = location,
            dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            imagePath = photoPath ?: "",
            rewardAmount = rewardStr.toDoubleOrNull() ?: 0.0,
            securityQuestion = question,
            securityAnswer = answer,
            status = "Available"
        )

        val result = dbHelper.insertListing(newListing)

        if (result != -1L) {
            Toast.makeText(this, "Item Posted Successfully!", Toast.LENGTH_SHORT).show()

            // REDIRECTION LOGIC
            val intent = Intent(this, MyListingsActivity::class.java)

            // Use Intent.FLAG_ACTIVITY_SINGLE_TOP to return to an existing instance if it exists
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(intent)
            finish() // Destroys PostItemActivity
        } else {
            Toast.makeText(this, "Database Error: Could not save listing", Toast.LENGTH_SHORT).show()
        }
    }
}

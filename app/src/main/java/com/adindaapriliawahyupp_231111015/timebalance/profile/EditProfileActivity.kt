package com.adindaapriliawahyupp_231111015.timebalance.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adindaapriliawahyupp_231111015.timebalance.R
import com.adindaapriliawahyupp_231111015.timebalance.data.SessionManager
import com.adindaapriliawahyupp_231111015.timebalance.database.DBAdapter
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var dbAdapter: DBAdapter
    private lateinit var sessionManager: SessionManager
    private var userId: Int = -1
    private var profileImageBytes: ByteArray? = null

    private lateinit var ivProfilePicture: ImageView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBio: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var ivEditPhoto: ImageView

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
        private const val PROFILE_IMAGE_SIZE = 300
        private const val MIN_PASSWORD_LENGTH = 8
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        dbAdapter = DBAdapter(this)
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()

        initializeViews()
        loadUserData()

        ivEditPhoto.setOnClickListener { pickImageFromGallery() }
        btnCancel.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveUserData() }
    }

    private fun initializeViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        ivEditPhoto = findViewById(R.id.ivEditPhoto)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etBio = findViewById(R.id.etBio)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun loadUserData() {
        val user = dbAdapter.getUserById(userId)
        if (user != null) {
            etFullName.setText(user["username"] as? String ?: "")
            etEmail.setText(user["email"] as? String ?: "")
            etPhone.setText(user["phone"] as? String ?: "")
            etBio.setText(user["bio"] as? String ?: "")

            val photoBytes = user["photo"] as? ByteArray
            if (photoBytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                ivProfilePicture.setImageBitmap(bitmap)
                profileImageBytes = photoBytes
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            val inputStream = contentResolver.openInputStream(uri!!)
            val selectedBitmap = BitmapFactory.decodeStream(inputStream)

            val resizedBitmap = Bitmap.createScaledBitmap(selectedBitmap, PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE, true)
            ivProfilePicture.setImageBitmap(resizedBitmap)

            val stream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            profileImageBytes = stream.toByteArray()
        }
    }

    private fun saveUserData() {
        val username = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val bio = etBio.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (!validateInputs(username, email, newPassword, confirmPassword)) {
            return
        }

        val updated = try {
            if (newPassword.isNotEmpty()) {
                // Update profile with new password (two separate operations)
                val profileUpdated = dbAdapter.updateUserProfile(
                    userId = userId,
                    username = username,
                    email = email,
                    phone = phone,
                    bio = bio,
                    photo = profileImageBytes
                )

                val passwordUpdated = dbAdapter.updateUserPassword(
                    userId = userId,
                    newPassword = newPassword
                )

                profileUpdated && passwordUpdated
            } else {
                // Update profile without changing password
                dbAdapter.updateUserProfile(
                    userId = userId,
                    username = username,
                    email = email,
                    phone = phone,
                    bio = bio,
                    photo = profileImageBytes
                )
            }
        } catch (e: Exception) {
            false
        }

        if (updated) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(
        username: String,
        email: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        if (username.isEmpty()) {
            etFullName.error = "Username cannot be empty"
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = "Email cannot be empty"
            return false
        }

        // Only validate password fields if new password is provided
        if (newPassword.isNotEmpty()) {
            if (newPassword.length < MIN_PASSWORD_LENGTH) {
                etNewPassword.error = "Password must be at least $MIN_PASSWORD_LENGTH characters"
                return false
            }

            if (confirmPassword.isEmpty()) {
                etConfirmPassword.error = "Please confirm your new password"
                return false
            }

            if (newPassword != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                return false
            }
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        dbAdapter.close()
    }
}
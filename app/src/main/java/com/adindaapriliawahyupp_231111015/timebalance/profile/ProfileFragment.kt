package com.adindaapriliawahyupp_231111015.timebalance.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adindaapriliawahyupp_231111015.timebalance.R
import com.adindaapriliawahyupp_231111015.timebalance.authentication.LoginActivity
import com.adindaapriliawahyupp_231111015.timebalance.data.SessionManager
import com.adindaapriliawahyupp_231111015.timebalance.database.DBAdapter
import com.adindaapriliawahyupp_231111015.timebalance.databinding.FragmentProfileBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbAdapter: DBAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        dbAdapter = DBAdapter(requireContext().applicationContext) // Gunakan applicationContext
        sessionManager = SessionManager(requireContext().applicationContext)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.layoutEditProfile.setOnClickListener {
            // Pastikan EditProfileActivity sudah diimpor dengan benar
            try {
                val intent = Intent(requireActivity(), EditProfileActivity::class.java)
                startActivity(intent)
                // Tambahkan animasi jika perlu
//                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } catch (e: Exception) {
                e.printStackTrace()
                // Tambahkan penanganan error atau log di sini
            }
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserData() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        try {
            dbAdapter.open()
            val user = dbAdapter.getUserById(userId)
            dbAdapter.close()

            if (user != null) {
                val username = user["username"] as? String ?: ""
                val email = user["email"] as? String ?: ""
                val bio = user["bio"] as? String ?: ""
                val phone = user["phone"] as? String ?: ""
                val createdAt = user["created_at"] as? String ?: ""
                val photoBytes = user["photo"] as? ByteArray?

                binding.tvName.text = username
                binding.tvEmail.text = email
                binding.tvBio.text = if (bio.isNotEmpty()) bio else "No bio available"
                binding.tvPhone.text = if (phone.isNotEmpty()) phone else "Not set"
                binding.tvJoinDate.text = formatDate(createdAt)

                if (photoBytes != null) {
                    val bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
                    binding.ivProfilePicture.setImageBitmap(bitmap)
                } else {
                    binding.ivProfilePicture.setImageResource(R.drawable.ic_person)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val date = parser.parse(dateString)
            formatter.format(date ?: Date())
        } catch (e: Exception) {
            ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
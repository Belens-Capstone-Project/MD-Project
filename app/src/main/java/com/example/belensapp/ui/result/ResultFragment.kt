package com.example.belensapp.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.belensapp.R
import com.example.belensapp.api.Gizi
import com.example.belensapp.api.PredictResponse
import com.example.belensapp.databinding.FragmentResultBinding
import com.example.belensapp.ui.history.HistoryViewModel
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.content.Context
import androidx.appcompat.app.AlertDialog

class ResultFragment : Fragment() {

    companion object {
        fun newInstance(predictResponse: PredictResponse, imageUri: Uri): ResultFragment {
            val fragment = ResultFragment()
            val bundle = Bundle()
            bundle.putParcelable("predict_response", predictResponse)
            bundle.putString("image_uri", imageUri.toString())
            fragment.arguments = bundle
            return fragment
        }
    }

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultBinding.inflate(inflater, container, false)

        // Initialize HistoryViewModel early
        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        // Show loading state
        showLoading(true)

        lifecycleScope.launch {
            try {
                val predictResponse = arguments?.getParcelable<PredictResponse>("predict_response")
                val imageUriString = arguments?.getString("image_uri")

                withContext(Dispatchers.Main) {
                    // Tambahkan pemanggilan checkAndUnlockBadges() di sini
                    checkAndUnlockBadges()

                    binding.tvPrediction.text = predictResponse?.data?.prediction ?: "Prediction not available"

                    // Display nutrition data
                    displayNutritionData(predictResponse?.data?.gizi)

                    if (imageUriString != null) {
                        val imageUri = Uri.parse(imageUriString)
                        loadImage(imageUri)
                        saveToHistoryWithTimeout(predictResponse, imageUri)
                    } else {
                        loadDefaultImage()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleError(e)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }

        return binding.root
    }

    // Fungsionalitas untuk menampilkan dan menyembunyikan loading
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.ivImageResult.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    // Memuat gambar dari URI
    private fun loadImage(imageUri: Uri) {
        Glide.with(requireContext())
            .load(imageUri)
            .placeholder(R.drawable.ic_camerafill)
            .error(R.drawable.ic_camerafill)
            .into(binding.ivImageResult)
    }

    private fun loadDefaultImage() {
        Glide.with(requireContext())
            .load(R.drawable.ic_camerafill)
            .into(binding.ivImageResult)
    }

    private suspend fun saveToHistoryWithTimeout(predictResponse: PredictResponse?, imageUri: Uri) {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", AppCompatActivity.MODE_PRIVATE)
        val userToken = sharedPref.getString("user_token", null)

        if (userToken != null && predictResponse != null) {
            val updatedResponse = predictResponse.copy(
                data = predictResponse.data?.copy(
                    fileUrl = imageUri.toString()
                )
            )

            // Add timeout of 10 seconds for save operation
            val result = withTimeoutOrNull(10000L) {
                try {
                    historyViewModel.savePredictionHistory(userToken, updatedResponse)
                    true
                } catch (e: Exception) {
                    false
                }
            }

            if (result == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Save operation timed out", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleError(error: Exception) {
        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
    }

    private fun displayNutritionData(gizi: Gizi?) {
        hideAllGradeImages()

        when (gizi?.grade?.uppercase()) {
            "A" -> binding.gradeA.visibility = View.VISIBLE
            "B" -> binding.gradeB.visibility = View.VISIBLE
            "C" -> binding.gradeC.visibility = View.VISIBLE
            "D" -> binding.gradeD.visibility = View.VISIBLE
            "E" -> binding.gradeE.visibility = View.VISIBLE
        }

        val giziText = buildString {
            append("Grade : ${gizi?.grade ?: "N/A"}\n")
            append("Energi (kkal): ${gizi?.totalEnergi ?: "N/A"}\n")
            append("Lemak Jenuh (g): ${gizi?.lemakJenuh ?: "N/A"}\n")
            append("Protein (g): ${gizi?.protein ?: "N/A"}\n")
            append("Gula (g): ${gizi?.gula ?: "N/A"}\n")
            append("Garam (mg): ${gizi?.garam ?: "N/A"}")
        }
        binding.tvGizi.text = giziText
        binding.recomendation.text = gizi?.rekomendasi ?: "Tidak ada rekomendasi tersedia"
    }

    private fun hideAllGradeImages() {
        binding.gradeA.visibility = View.GONE
        binding.gradeB.visibility = View.GONE
        binding.gradeC.visibility = View.GONE
        binding.gradeD.visibility = View.GONE
        binding.gradeE.visibility = View.GONE
    }

    // Fungsi untuk cek dan membuka badge berdasarkan jumlah scan
    private fun checkAndUnlockBadges() {
        val sharedPref = requireContext().getSharedPreferences("badge_status", Context.MODE_PRIVATE)
        val scanCount = sharedPref.getInt("scan_count", 0) + 1

        with(sharedPref.edit()) {
            putInt("scan_count", scanCount)
            apply()
        }

        val newBadges = mutableListOf<String>()

        when {
            scanCount == 1 -> {
                saveBadgeStatus(1, true)
                newBadges.add("Sugar Novice")
            }
            scanCount == 5 -> {
                saveBadgeStatus(2, true)
                newBadges.add("Mindful Drinker")
            }
            scanCount == 10 -> {
                saveBadgeStatus(3, true)
                newBadges.add("Health Conscious")
            }
            scanCount == 20 -> {
                saveBadgeStatus(4, true)
                newBadges.add("Sugar Savvy")
            }
            scanCount == 50 -> {
                saveBadgeStatus(5, true)
                newBadges.add("Healthy Expert")
            }
        }

        if (newBadges.isNotEmpty()) {
            showBadgeAchievementDialog(newBadges)
        }
    }

    private fun showBadgeAchievementDialog(badges: List<String>) {
        val badgeNames = badges.joinToString(", ")
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("🏆 Badge Achievement!")
        dialogBuilder.setMessage("Congratulations! You've unlocked:\n$badgeNames")
        dialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.create().show()
    }

    private fun saveBadgeStatus(badgeNumber: Int, isAchieved: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("badge_status", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("badge_$badgeNumber", isAchieved)
            apply()
        }
    }

    private fun updateScanCount() {
        val sharedPref = requireActivity().getSharedPreferences("badge_status", AppCompatActivity.MODE_PRIVATE)
        val scanCount = sharedPref.getInt("scan_count", 0) + 1

        with(sharedPref.edit()) {
            putInt("scan_count", scanCount)
            apply()
        }

        when (scanCount) {
            1 -> saveBadgeStatus(1, true)
            5 -> saveBadgeStatus(2, true)
            10 -> saveBadgeStatus(3, true)
            20 -> saveBadgeStatus(4, true)
            50 -> saveBadgeStatus(5, true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<View>(R.id.nav_view)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
        _binding = null
    }
}

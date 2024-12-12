package com.example.belensapp.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.belensapp.MainActivity
import com.example.belensapp.R
import com.example.belensapp.databinding.FragmentDetailHistoryBinding
import com.example.belensapp.api.Gizi
import com.example.belensapp.api.PredictResponse

class DetailHistoryFragment : Fragment() {

    private var _binding: FragmentDetailHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavigationVisibility(false)

        val predictResponse = arguments?.getParcelable<PredictResponse>("predict_response")

        if (predictResponse != null) {
            binding.tvPrediction.text = predictResponse.data?.prediction ?: "Prediction not available"

            displayGradeImage(predictResponse.data?.gizi?.grade)

            displayNutritionData(predictResponse.data?.gizi)

            val imageUrl = predictResponse.data?.fileUrl
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .into(binding.ivImageHistory)
        } else {
            binding.tvPrediction.text = "No data available"
        }
    }

    private fun displayGradeImage(grade: String?) {
        binding.gradeA.visibility = View.GONE
        binding.gradeB.visibility = View.GONE
        binding.gradeC.visibility = View.GONE
        binding.gradeD.visibility = View.GONE
        binding.gradeE.visibility = View.GONE

        when (grade?.uppercase()) {
            "A" -> binding.gradeA.visibility = View.VISIBLE
            "B" -> binding.gradeB.visibility = View.VISIBLE
            "C" -> binding.gradeC.visibility = View.VISIBLE
            "D" -> binding.gradeD.visibility = View.VISIBLE
            "E" -> binding.gradeE.visibility = View.VISIBLE
        }
    }

    private fun displayNutritionData(gizi: Gizi?) {
        if (gizi == null) {
            binding.tvGizi.text = "No nutrition data available"
            binding.recomendation.text = "No recommendation available"
            return
        }

        val giziText = buildString {
            append("Grade : ${gizi.grade ?: "N/A"}\n")
            append("Energi (kkal): ${gizi.totalEnergi ?: "N/A"}\n")
            append("Lemak Jenuh (g): ${gizi.lemakJenuh ?: "N/A"}\n")
            append("Protein (g): ${gizi.protein ?: "N/A"}\n")
            append("Gula (g): ${gizi.gula ?: "N/A"}\n")
            append("Garam (mg): ${gizi.garam ?: "N/A"}\n")
        }
        binding.tvGizi.text = giziText

        binding.recomendation.text = gizi.rekomendasi ?: "No recommendation available"
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as? MainActivity)?.setBottomNavigationVisibility(true)

        _binding = null
    }
}
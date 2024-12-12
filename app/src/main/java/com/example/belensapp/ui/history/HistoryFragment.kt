package com.example.belensapp.ui.history

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.belensapp.R
import com.example.belensapp.databinding.FragmentHistoryBinding
import com.example.belensapp.utils.Constants

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        binding.progressBar.visibility = View.VISIBLE

        historyAdapter = HistoryAdapter(emptyList()) { predictResponse ->
            val bundle = Bundle().apply {
                putParcelable("predict_response", predictResponse)
            }
            findNavController().navigate(
                R.id.action_navigation_history_to_detailHistoryFragment,
                bundle
            )
        }

        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        val sharedPref =
            requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        val userToken = sharedPref.getString(Constants.KEY_USER_TOKEN, null)

        if (userToken == null) {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = "Please login to view history"
            binding.progressBar.visibility = View.GONE
            return root
        }

        historyViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = it
                binding.progressBar.visibility = View.GONE
            }
        }

        historyViewModel.historyList.observe(viewLifecycleOwner) { historyList ->
            binding.progressBar.visibility = View.GONE
            if (historyList.isEmpty()) {
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = "No history available"
            } else {
                binding.tvError.visibility = View.GONE
                historyAdapter.updateHistoryList(historyList)
            }
        }

        historyViewModel.fetchHistory(userToken)

        return root
    }
}

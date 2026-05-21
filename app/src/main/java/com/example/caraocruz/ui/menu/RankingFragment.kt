package com.example.caraocruz.ui.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentRankingBinding
import kotlinx.coroutines.launch

class RankingFragment : Fragment(R.layout.fragment_ranking) {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RankingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRankingBinding.bind(view)

        binding.rvRanking.layoutManager = LinearLayoutManager(requireContext())
        
        binding.btnRetryRanking.setOnClickListener {
            viewModel.refresh()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is RankingState.Loading -> {
                            binding.pbRanking.visibility = View.VISIBLE
                            binding.llErrorRanking.visibility = View.GONE
                        }
                        is RankingState.Success -> {
                            binding.pbRanking.visibility = View.GONE
                            binding.llErrorRanking.visibility = View.GONE
                            binding.rvRanking.adapter = RankingAdapter(state.ranking)
                        }
                        is RankingState.Error -> {
                            binding.pbRanking.visibility = View.GONE
                            binding.llErrorRanking.visibility = View.VISIBLE
                            binding.tvErrorRanking.text = state.message
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

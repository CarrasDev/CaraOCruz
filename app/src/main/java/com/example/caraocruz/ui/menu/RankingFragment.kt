package com.example.caraocruz.ui.menu

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caraocruz.R
import com.example.caraocruz.data.api.RetrofitClient
import com.example.caraocruz.databinding.FragmentRankingBinding
import kotlinx.coroutines.launch

class RankingFragment : Fragment(R.layout.fragment_ranking) {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRankingBinding.bind(view)

        binding.rvRanking.layoutManager = LinearLayoutManager(requireContext())
        
        cargarRanking()
    }

    private fun cargarRanking() {
        binding.pbRanking.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val ranking = RetrofitClient.rankingInstance.obtenerRanking()
                binding.rvRanking.adapter = RankingAdapter(ranking)
            } catch (e: Exception) {
                Log.e("RankingFragment", "Error al cargar ranking", e)
            } finally {
                binding.pbRanking.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

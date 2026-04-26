package com.example.caraocruz.ui.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caraocruz.R
import com.example.caraocruz.data.AppDatabase
import com.example.caraocruz.data.JuegoRepository
import com.example.caraocruz.databinding.FragmentHistoryBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val repository by lazy { JuegoRepository(database.juegoDao()) }
    private val viewModel: HistoryViewModel by viewModels { HistoryViewModelFactory(repository) }
    private val disposables = CompositeDisposable()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupRecyclerView()
        subscribeToHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter
    }

    private fun subscribeToHistory() {
        disposables.add(
            viewModel.getAllPartidas()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { partidas ->
                    if (partidas.isEmpty()) {
                        binding.rvHistory.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvHistory.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        historyAdapter.submitList(partidas)
                    }
                }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        disposables.clear()  // Limpiar los observables
    }
}
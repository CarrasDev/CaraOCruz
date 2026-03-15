package com.example.caraocruz.ui.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caraocruz.R
import com.example.caraocruz.databinding.FragmentHistoryBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    // TODO: Crear el ViewModel para consultar el historial de jugadas
    // TODO: El ViewModel conecta con el DAO para recuperar los datos
    // private val viewModel: HistoryViewModel by viewModels()

    private val disposables = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupRecyclerView()
        subscribeToHistory()
    }

    private fun setupRecyclerView() {
        // binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Configurar el adaptador para el RecyclerView
        // binding.rvHistory.adapter = HistoryAdapter()
    }

    private fun subscribeToHistory() {
        // TODO: recuperar los datos del historial con RxJava
        /*
        disposables.add(
            viewModel.getAllApuestas()
                .observeOn(AndroidSchedulers.mainThread())
                .suscribe { lista ->
                    // Actualizar el adaptador con los datos recuperados
                    (binding.rvHistory.adapter as HistoryAdapter).submitList(lista)
                }
         )
         */


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        disposables.clear()  // Limpiar los observables
    }
}
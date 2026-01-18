// en app/src/main/java/ve/com/movilnet/ui/Fragments/NumerosFragment.kt
package ve.com.movilnet.ui.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager // <-- IMPORTACIÓN NECESARIA
import ve.com.movilnet.databinding.FragmentNumerosBinding
import ve.com.movilnet.ui.Adapters.NumerosAdapter       // <-- IMPORTACIÓN NECESARIA
import ve.com.movilnet.ui.viewmodel.NumerosViewModel

class NumerosFragment : Fragment() {

    private var _binding: FragmentNumerosBinding? = null
    private val binding get() = _binding!!

    private val numerosViewModel: NumerosViewModel by viewModels()
    private lateinit var numerosAdapter: NumerosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNumerosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- PASO 1: LLAMAR A LA CONFIGURACIÓN DEL RECYCLERVIEW ---
        setupRecyclerView() // Esta línea es fundamental para que todo funcione.

        setupSearch()
        setupObservers()

        if (numerosViewModel.numeros.value.isNullOrEmpty()) {
            numerosViewModel.cargarNumeros()
        }
    }

    // --- PASO 2: DESCOMENTAR Y ACTIVAR ESTE MÉTODO ---
    private fun setupRecyclerView() {
        // Inicializa el adaptador con una lista vacía.
        numerosAdapter = NumerosAdapter(emptyList())

        // Configura el RecyclerView con su LayoutManager y el adaptador recién creado.
        binding.recyclerViewNumeros.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = numerosAdapter
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { query ->
            numerosViewModel.filtrarLista(query.toString())
        }
    }

    private fun setupObservers() {
        numerosViewModel.numeros.observe(viewLifecycleOwner, Observer { listaDeNumeros ->
            // Esta línea ahora es segura porque `numerosAdapter` ya está inicializado.
            numerosAdapter.updateList(listaDeNumeros)
        })

        numerosViewModel.isLoading.observe(viewLifecycleOwner, Observer { estaCargando ->
            binding.recyclerViewNumeros.isVisible = !estaCargando
        })

        numerosViewModel.error.observe(viewLifecycleOwner, Observer { mensajeError ->
            Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

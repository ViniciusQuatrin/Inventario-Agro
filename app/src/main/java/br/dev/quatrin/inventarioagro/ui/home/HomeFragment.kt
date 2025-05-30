package br.dev.quatrin.inventarioagro.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.dev.quatrin.inventarioagro.R
import br.dev.quatrin.inventarioagro.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupButtons()
    }
    
    private fun setupButtons() {
        binding.btnMaquinas.setOnClickListener {
        }
        
        binding.btnTipos.setOnClickListener {
        }
        
        binding.btnMarcas.setOnClickListener {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.bookchat.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.CompleteBookTabAdapter
import com.example.bookchat.databinding.FragmentCompleteBookTabBinding

class CompleteBookTabFragment : Fragment() {
    private lateinit var binding : FragmentCompleteBookTabBinding
    private lateinit var completeBookAdapter :CompleteBookTabAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_complete_book_tab,container,false)
        with(binding){
            lifecycleOwner = this@CompleteBookTabFragment
        }
        initRecyclerView()

        return binding.root
    }

    private fun initRecyclerView(){
        with(binding){
            completeBookAdapter = CompleteBookTabAdapter()
            completeRcv.adapter = completeBookAdapter
            completeRcv.setHasFixedSize(true)
            completeRcv.layoutManager = LinearLayoutManager(requireContext())
        }
    }

}
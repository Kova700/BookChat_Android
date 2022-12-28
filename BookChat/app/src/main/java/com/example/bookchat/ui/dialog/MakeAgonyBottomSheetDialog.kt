package com.example.bookchat.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.bookchat.R
import com.example.bookchat.databinding.DialogMakeAgonyBottomSheetBinding
import com.example.bookchat.viewmodel.AgonyViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MakeAgonyBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var binding :DialogMakeAgonyBottomSheetBinding
    val agonyViewModel: AgonyViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.dialog_make_agony_bottom_sheet,container,false)
        binding.viewmodel = agonyViewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}
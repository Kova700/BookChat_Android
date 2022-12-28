package com.example.bookchat.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.databinding.DialogMakeAgonyBottomSheetBinding
import com.example.bookchat.viewmodel.MakeAgonyDialogViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MakeAgonyBottomSheetDialog(private val book: BookShelfItem) : BottomSheetDialogFragment() {

    @Inject
    lateinit var makeAgonyDialogViewModelFactory : MakeAgonyDialogViewModel.AssistedFactory

    private lateinit var binding :DialogMakeAgonyBottomSheetBinding
    private val makeAgonyDialogViewModel: MakeAgonyDialogViewModel by viewModels{
        MakeAgonyDialogViewModel.provideFactory(makeAgonyDialogViewModelFactory, book)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.dialog_make_agony_bottom_sheet,container,false)
        binding.viewmodel = makeAgonyDialogViewModel
        binding.lifecycleOwner = this

        return binding.root
    }
}
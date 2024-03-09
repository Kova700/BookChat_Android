package com.example.bookchat.ui.agony

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.databinding.DialogMakeAgonyBottomSheetBinding
import com.example.bookchat.ui.agony.AgonyViewModel
import com.example.bookchat.ui.agony.MakeAgonyDialogViewModel
import com.example.bookchat.ui.agony.MakeAgonyDialogViewModel.MakeAgonyUiEvent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MakeAgonyBottomSheetDialog(private val book: BookShelfItem) : BottomSheetDialogFragment() {

    @Inject
    lateinit var makeAgonyDialogViewModelFactory : MakeAgonyDialogViewModel.AssistedFactory

    private lateinit var binding :DialogMakeAgonyBottomSheetBinding
    private val makeAgonyDialogViewModel: MakeAgonyDialogViewModel by viewModels{
        MakeAgonyDialogViewModel.provideFactory(makeAgonyDialogViewModelFactory, book)
    }

    private val agonyViewModel: AgonyViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.dialog_make_agony_bottom_sheet,container,false)
        binding.viewmodel = makeAgonyDialogViewModel
        binding.lifecycleOwner = this
        observeMakeAgonyUiEvent()

        return binding.root
    }

    private fun observeMakeAgonyUiEvent() = lifecycleScope.launch{
        makeAgonyDialogViewModel.eventFlow.collect{ event -> handleEvent(event) }
    }

    private fun handleEvent(event : MakeAgonyUiEvent){
        when(event){
            is MakeAgonyUiEvent.RenewAgonyList -> {
                //다이얼로그 닫고
                this.dismiss()
                //고민기록탭에 데이터 갱신 혹은 데이터가 추가되게 화면 갱신이 필요함
                agonyViewModel.renewAgonyList()
            }
        }
    }
}
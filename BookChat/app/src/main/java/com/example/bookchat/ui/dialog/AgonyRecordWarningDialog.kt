package com.example.bookchat.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.bookchat.R
import com.example.bookchat.data.AgonyRecordFirstItemStatus
import com.example.bookchat.databinding.DialogAgonyRecordWarningBinding
import com.example.bookchat.viewmodel.AgonyRecordViewModel
import com.example.bookchat.viewmodel.AgonyRecordViewModel.AgonyRecordUiState

class AgonyRecordWarningDialog : DialogFragment() {

    private lateinit var binding: DialogAgonyRecordWarningBinding
    private val agonyRecordViewModel : AgonyRecordViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_agony_record_warning, container, false)
        binding.dialog = this
        binding.lifecycleOwner = this
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

    fun clickCancelBtn(){
        this.dismiss()
    }

    fun clickOkBtn(){
        with(agonyRecordViewModel){
            renewFirstItemUi(AgonyRecordFirstItemStatus.Default)
            clearFirstItemData()
            resetAllEditingItemToDefault()
            setUiState(AgonyRecordUiState.Default)
        }
        this.dismiss()
    }
}
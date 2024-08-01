package com.example.bookchat.ui.search.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.bookchat.databinding.DialogCompleteBookSetStarsBinding

class CompleteBookStarSetDialog : DialogFragment() {

	private var _binding: DialogCompleteBookSetStarsBinding? = null
	private val binding get() = _binding!!

	private val searchBookDialogViewModel: SearchBookDialogViewModel by viewModels({
		requireParentFragment()
	})

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogCompleteBookSetStarsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun initViewState() {
		initScaleRatingBar()
		binding.cancelBtn.setOnClickListener { onClickCancelBtn() }
		binding.completeBtn.setOnClickListener { onClickCompleteOkBtn() }
	}

	private fun initScaleRatingBar() {
		binding.starRatingBar.setOnRatingChangeListener { _, rating, _ ->
			searchBookDialogViewModel.onChangeStarRating(rating)
		}
	}

	fun onClickCancelBtn() {
		dismiss()
	}

	fun onClickCompleteOkBtn() {
		searchBookDialogViewModel.onClickCompleteOkBtn()
		dismiss()
	}
}
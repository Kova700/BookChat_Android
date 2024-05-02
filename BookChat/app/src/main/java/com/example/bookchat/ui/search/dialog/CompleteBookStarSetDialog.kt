package com.example.bookchat.ui.search.dialog

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
		savedInstanceState: Bundle?
	): View {
		_binding =
			DataBindingUtil.inflate(inflater, R.layout.dialog_complete_book_set_stars, container, false)
		binding.lifecycleOwner = this
		binding.dialog = this
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		initScaleRatingBar()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun initScaleRatingBar() {
		binding.starRatingBar.setOnRatingChangeListener { _, rating, _ ->
			searchBookDialogViewModel.onStarRatingChange(rating)
		}
	}

	fun onClickCancelBtn() {
		this.dismiss()
	}

	fun onClickCompleteOkBtn() {
		searchBookDialogViewModel.onClickCompleteOkBtn()
		this.dismiss()
	}
}
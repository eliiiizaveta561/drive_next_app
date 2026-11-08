package com.drivenext.app.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.drivenext.app.R
import com.drivenext.app.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentOnboardingBinding.bind(view)
        val args = requireArguments()

        binding.ivSlide.setImageResource(args.getInt(ARG_IMAGE))
        binding.tvTitle.setText(args.getInt(ARG_TITLE))
        binding.tvDescription.setText(args.getInt(ARG_DESCRIPTION))
    }

    companion object {
        private const val ARG_IMAGE = "image"
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"

        fun newInstance(slide: OnboardingSlide) = OnboardingFragment().apply {
            arguments = bundleOf(
                ARG_IMAGE to slide.imageRes,
                ARG_TITLE to slide.titleRes,
                ARG_DESCRIPTION to slide.descriptionRes
            )
        }
    }
}

package com.drivenext.app.ui.onboarding

import com.drivenext.app.R

data class OnboardingSlide(
    val imageRes: Int,
    val titleRes: Int,
    val descriptionRes: Int
)

val onboardingSlides = listOf(
    OnboardingSlide(R.drawable.onboarding_1, R.string.onboarding_title_1, R.string.onboarding_desc_1),
    OnboardingSlide(R.drawable.onboarding_2, R.string.onboarding_title_2, R.string.onboarding_desc_2),
    OnboardingSlide(R.drawable.onboarding_3, R.string.onboarding_title_3, R.string.onboarding_desc_3)
)

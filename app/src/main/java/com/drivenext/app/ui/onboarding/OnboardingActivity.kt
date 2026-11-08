package com.drivenext.app.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.viewpager2.widget.ViewPager2
import com.drivenext.app.R
import com.drivenext.app.databinding.ActivityOnboardingBinding
import com.drivenext.app.ui.common.openScreen
import com.drivenext.app.ui.welcome.WelcomeActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = OnboardingPagerAdapter(this)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = showPage(position)
        })

        binding.btnNext.setOnClickListener { showNextPage() }
        binding.tvSkip.setOnClickListener { finishOnboarding() }
    }

    private fun showPage(position: Int) {
        val indicators = listOf(binding.ind1, binding.ind2, binding.ind3)
        indicators.forEachIndexed { index, indicator -> setIndicatorActive(indicator, index == position) }
        binding.btnNext.setText(if (isLastPage(position)) R.string.lets_go else R.string.next)
    }

    private fun showNextPage() {
        val current = binding.viewPager.currentItem
        if (isLastPage(current)) {
            finishOnboarding()
        } else {
            binding.viewPager.currentItem = current + 1
        }
    }

    private fun isLastPage(position: Int) = position == onboardingSlides.lastIndex

    private fun setIndicatorActive(indicator: View, isActive: Boolean) {
        val widthRes = if (isActive) R.dimen.indicator_active_width else R.dimen.indicator_width
        indicator.updateLayoutParams { width = resources.getDimensionPixelSize(widthRes) }
        indicator.setBackgroundResource(if (isActive) R.drawable.indicator_active else R.drawable.indicator_inactive)
    }

    private fun finishOnboarding() {
        viewModel.finishOnboarding()
        openScreen(WelcomeActivity::class.java)
        finish()
    }
}

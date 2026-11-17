package com.drivenext.app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.drivenext.app.R
import com.drivenext.app.databinding.ActivityMainBinding
import com.drivenext.app.ui.favorites.FavoritesFragment
import com.drivenext.app.ui.home.HomeFragment
import com.drivenext.app.ui.settings.SettingsFragment

/**
 * Главный экран-контейнер: сверху фрагмент текущей вкладки, снизу BottomNavigationView.
 * Пункты меню описаны в res/menu/bottom_nav.xml.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            showTab(tabFragment(item.itemId))
            true
        }
        // Повторное нажатие на активную вкладку возвращает к её первому экрану
        binding.bottomNav.setOnItemReselectedListener { clearTabBackStack() }

        // После поворота экрана фрагменты восстанавливаются сами — создаём только при первом запуске
        if (savedInstanceState == null) {
            showTab(HomeFragment())
        }
    }

    /** Открывает вложенный экран внутри текущей вкладки («Назад» вернёт на предыдущий). */
    fun openInCurrentTab(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.container, fragment)
            addToBackStack(null)
        }
    }

    private fun showTab(fragment: Fragment) {
        clearTabBackStack()
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.container, fragment)
        }
    }

    private fun clearTabBackStack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun tabFragment(itemId: Int): Fragment = when (itemId) {
        R.id.nav_favorites -> FavoritesFragment()
        R.id.nav_settings -> SettingsFragment()
        else -> HomeFragment()
    }
}

fun Fragment.openInCurrentTab(fragment: Fragment) {
    (requireActivity() as MainActivity).openInCurrentTab(fragment)
}

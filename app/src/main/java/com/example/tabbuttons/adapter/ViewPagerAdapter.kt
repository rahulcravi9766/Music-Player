package com.example.tabbuttons.adapter

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tabbuttons.fragments.PlaylistFragment
import com.example.tabbuttons.fragments.HomeFragment

//connecting the fragments with the viewAdapter
class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle,
                       private val view: View
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    //returning total number of fragments
    override fun getItemCount(): Int {
        return 2
    }

    //creating fragment based on the position number
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                HomeFragment(view)
            }
            1 -> {
                PlaylistFragment(view)
            }

            else -> {
                Fragment()
            }
        }
    }
}
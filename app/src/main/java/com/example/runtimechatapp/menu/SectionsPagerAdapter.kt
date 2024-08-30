package com.example.runtimechatapp.menu

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SectionsPagerAdapter(activity: AppCompatActivity): FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? =null
        when(position){
            0-> fragment = ChatFragment()
            1-> fragment = ContactFragment()
            2-> fragment = SettingFragment()
        }
        return fragment as Fragment
    }

    override fun getItemCount(): Int {
        return 3
    }

}
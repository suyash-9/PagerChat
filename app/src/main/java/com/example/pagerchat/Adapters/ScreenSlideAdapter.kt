package com.example.pagerchat.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pagerchat.InboxFragment
import com.example.pagerchat.PeopleFragment

class ScreenSlideAdapter(fa:FragmentActivity):FragmentStateAdapter(fa) {
    override fun getItemCount(): Int =2



    override fun createFragment(position: Int): Fragment =when(position){
        0-> InboxFragment()
        else-> PeopleFragment()
    }

}
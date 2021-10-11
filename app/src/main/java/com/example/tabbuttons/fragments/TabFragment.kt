package com.example.tabbuttons.fragments

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.tabbuttons.R
import com.example.tabbuttons.databinding.FragmentTabBinding
import com.example.tabbuttons.adapter.ViewPagerAdapter
import com.example.tabbuttons.model.musicService
import com.example.tabbuttons.service.PlayMusicService
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator

class TabFragment : Fragment(), ServiceConnection {

    companion object {
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
    }

    lateinit var tabBinding: FragmentTabBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val navController = findNavController()
        val intent = Intent(activity, PlayMusicService::class.java)
        activity?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        activity?.startService(intent)

        tabBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_tab, container, false)


        if (musicService != null) checkingSleepTimerOnOrOff()

        val viewPagerAdapter =
            activity?.let {
                ViewPagerAdapter(
                    it.supportFragmentManager,
                    lifecycle,
                    tabBinding.root
                )
            }
        tabBinding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(tabBinding.tabLayout, tabBinding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Songs"
                }
                1 -> {
                    tab.text = "Playlists"
                }
            }
        }.attach()

        tabBinding.playPauseButtonBottom.setOnClickListener {
            if (musicService!!.mediaPlayer!!.isPlaying) musicService!!.pauseMusic()
            else musicService!!.playMusic()
        }

        tabBinding.cardView.setOnClickListener {
            val actions =
                TabFragmentDirections.actionTabFragmentToMusicPlayerFragment(
                    musicService!!.songPosition,
                    musicService!!.currentSongId
                )
            navController.navigate(actions)
        }

        tabBinding.sleepTimerButton.setOnClickListener {
            val timer = min15 || min30 || min60

            if (!timer) {
                showBottomSheetDialog()
            } else {
                val builder = MaterialAlertDialogBuilder(requireContext())
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop timer?")
                    .setPositiveButton("Yes") { _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false

                        Toast.makeText(
                            requireContext(),
                            "Sleep timer switched off",
                            Toast.LENGTH_SHORT
                        ).show()
                        tabBinding.sleepTimerButton.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.black
                            )
                        )
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }

                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }


        if (musicService != null && musicService!!.mediaPlayer != null) {
            tabBinding.cardView.visibility = View.VISIBLE
            tabBinding.songNameBottom.text =
                musicService!!.listOfSongs[musicService!!.songPosition].songName
            tabBinding.songNameBottom.isSelected = true

            if (musicService!!.mediaPlayer!!.isPlaying) {
                tabBinding.playPauseButtonBottom.setImageResource(R.drawable.ic_pause_bottom)
            } else {
                tabBinding.playPauseButtonBottom.setImageResource(R.drawable.ic_play_bottom)
            }
        }
        return tabBinding.root
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.bottom_sheet)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.lay_one)?.setOnClickListener {
            musicService!!.sleepButton = true
            Toast.makeText(requireContext(), "Sleep timer set for 15 min", Toast.LENGTH_SHORT)
                .show()
            min15 = true
            Thread {

                Thread.sleep(900000)
                musicService!!.sleepButton = false
                if (musicService!!.mediaPlayer != null && musicService!!.mainActivity.get().let { it?.isDestroyed == true }) {
                    min15 = false
                    exitProcessForSleeperTime()
                } else if (this.isVisible) {
                    min15 = false
                    changeSleepTimerColor()
                }
            }.start()
            dialog.dismiss()

            tabBinding.sleepTimerButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green
                )
            )
        }

        dialog.findViewById<LinearLayout>(R.id.lay_two)?.setOnClickListener {
            musicService!!.sleepButton = true
            Toast.makeText(requireContext(), "Sleep timer set for 30 min", Toast.LENGTH_SHORT)
                .show()
            min30 = true
            Thread {

                Thread.sleep(1800000)
                musicService!!.sleepButton = false
                if (musicService!!.mediaPlayer != null && musicService!!.mainActivity.get().let { it?.isDestroyed == true }) {
                    exitProcessForSleeperTime()
                } else if (this.isVisible) {

                    tabBinding.sleepTimerButton.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )

                }
            }.start()
            dialog.dismiss()

            tabBinding.sleepTimerButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green
                )
            )
        }

        dialog.findViewById<LinearLayout>(R.id.lay_three)?.setOnClickListener {
            musicService!!.sleepButton = true
            Toast.makeText(requireContext(), "Sleep timer set for 60 min", Toast.LENGTH_SHORT)
                .show()
            min60 = true
            Thread {

                Thread.sleep(3600000)
                musicService!!.sleepButton = false
                if (musicService!!.mediaPlayer != null && musicService!!.mainActivity.get().let { it?.isDestroyed == true }) {
                    exitProcessForSleeperTime()
                } else if (this.isVisible) {

                    tabBinding.sleepTimerButton.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                }
            }.start()
            dialog.dismiss()

            tabBinding.sleepTimerButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green
                )
            )
        }
    }

    private fun checkingSleepTimerOnOrOff() {
        if (musicService!!.sleepButton) {
            tabBinding.sleepTimerButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green
                )
            )
        } else {
            tabBinding.sleepTimerButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }
    }


    private fun exitProcessForSleeperTime() {

        musicService!!.mediaPlayer!!.pause()
        musicService!!.mediaPlayer!!.stop()
        musicService!!.stopForeground(true)
    }

    private fun changeSleepTimerColor() {

//        musicService!!.mainActivity.runOnUiThread {
            tabBinding.sleepTimerButton.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
     //   }
    }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as PlayMusicService.MyBinder
        musicService = binder.currentService()
        checkingSleepTimerOnOrOff()
        musicService!!.tabCalling(this)
        musicService!!.readFavSongs("favorite")

    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}
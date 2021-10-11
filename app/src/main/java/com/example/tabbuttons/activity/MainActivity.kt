package com.example.tabbuttons.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.tabbuttons.R
import com.example.tabbuttons.model.musicService
import com.example.tabbuttons.service.PlayMusicService
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity(), ServiceConnection

{

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, PlayMusicService::class.java)
        this.bindService(intent, this, Context.BIND_AUTO_CREATE)
        this.startService(intent)

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.myNavHostFragment) as NavHostFragment? ?: return

        val navController = host.navController


        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.tabFragment)
        ) //  IDs of fragments you want without the ActionBar home/up button

        setupActionBarWithNavController(navController, appBarConfiguration)


//        supportActionBar!!.setBackgroundDrawable(
//            ColorDrawable(resources.getColor(R.color.black)))
        }

    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as PlayMusicService.MyBinder
        musicService = binder.currentService()
        val weakReferenceMainActivity = WeakReference(this)
        musicService!!.mainActivity = weakReferenceMainActivity
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

}


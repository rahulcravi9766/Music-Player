package com.example.tabbuttons.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.tabbuttons.R
import com.example.tabbuttons.model.musicService


class MainActivity : AppCompatActivity()
    //CoroutineScope
{

//lateinit var  job : Job
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val PERMISSION_REQUEST_CODE = 101
  //  private lateinit var mainBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        job = Job()
//        launch {  }

//        if (
//                ContextCompat.checkSelfPermission(
//
//                    applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE
//                )
//
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            Log.i("Permission", "checking permission")
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_PHONE_STATE
//                ),
//                PERMISSION_REQUEST_CODE
//            )
////            loadData()
//        }


        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.myNavHostFragment) as NavHostFragment? ?: return

        val navController = host.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.tabFragment)
        ) //  IDs of fragments you want without the ActionBar home/up button

        setupActionBarWithNavController(navController, appBarConfiguration)


        supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(resources.getColor(R.color.black)))
        }

//    override val coroutineContext: CoroutineContext
//        get() = Dispatchers.Main + job


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.more_menu,menu)
//        return true
//
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        job.cancel()
//    }


}


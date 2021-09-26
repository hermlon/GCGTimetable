package net.hermlon.gcgtimetable

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import net.hermlon.gcgtimetable.databinding.ActivityMainBinding
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    var profileMenuIds: MutableMap<Int, Long> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val viewModel: MainViewModel by viewModels()
        binding.viewModel = viewModel

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_settings
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        // navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            //drawerLayout.closeDrawer(navView)
            if(item.itemId in profileMenuIds.keys) {
                navView.setCheckedItem(item.itemId)
                item.setChecked(true)
                Toast.makeText(this, item.itemId.toString(), Toast.LENGTH_SHORT).show()
                return@OnNavigationItemSelectedListener true
            }
            else {
                return@OnNavigationItemSelectedListener NavigationUI.onNavDestinationSelected(item, navController)
            }
        })


        /*
        val weakReference =
            WeakReference<NavigationView>(navView)
        navController.addOnDestinationChangedListener ( NavController.OnDestinationChangedListener listener@ { controller: NavController, destination: NavDestination, arguments: Bundle? ->
            val view: NavigationView? = weakReference.get()
            if (view == null) {
                navController.removeOnDestinationChangedListener(this@listener)
            } else {
                var menu: Menu = view.getMenu()
                menu.forEach { item ->
                    item.isChecked = matchDestination(destination, item.getItemId())
                }
            }
        }
        )
*/
        viewModel.profiles.observe(this, Observer { profiles ->
            profileMenuIds.clear()
            // remove previous profile entries
            navView.menu.children.first().subMenu.clear()

            profiles.forEachIndexed { index, profile ->
                val id = View.generateViewId()
                profileMenuIds[id] = profile.id
                navView.menu.children.first().subMenu.add(R.id.menu_profiles, id, index, profile.name)
            }
        })
    }

    /* copied from NavigationUI */
    private fun matchDestination(
        destination: NavDestination,
        @IdRes destId: Int
    ): Boolean {
        var currentDestination: NavDestination? = destination
        while (currentDestination!!.id != destId && currentDestination.parent != null) {
            currentDestination = currentDestination.parent
        }
        return currentDestination.id == destId
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}

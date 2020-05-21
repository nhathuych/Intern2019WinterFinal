package com.asiantech.intern2019winterfinal.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.fragment.*
import com.asiantech.intern2019winterfinal.model.User
import com.asiantech.intern2019winterfinal.utils.MyUtil
import com.asiantech.intern2019winterfinal.utils.SharedPrefs
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.nav_header.view.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_MANAGER = "manager"
        const val ROLE_USER = "user"
    }

    private var sharedPrefs = SharedPrefs(this)
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        currentUser = MyUtil.getSessionUser()
        currentUser?.fullName?.let {
            // app:headerLayout="@layout/nav_header" add bằng code kotlin
            navigationView.inflateHeaderView(R.layout.nav_header).tvUserName.text = it
        }

        initActionBar()
        supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContent, TablesFragment()).commit()
    }

    private fun initActionBar() {
        navigationView.setNavigationItemSelectedListener(this)
        setSupportActionBar(toolbar)
        val hamburger = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawerlayout_open, R.string.drawerlayout_close)
        drawerLayout.addDrawerListener(hamburger)
        hamburger.syncState()
        supportActionBar?.setTitle(R.string.menu_tables)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menuTable -> {
                supportActionBar?.setTitle(R.string.menu_tables)
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContent, TablesFragment())
                    .addToBackStack("TablesFragment").commit()
            }
            R.id.menuMenu -> {
                supportActionBar?.setTitle(R.string.menu_menu)
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContent, MenuFragment())
                    .addToBackStack("MenuFragment").commit()
            }
            R.id.menuBill -> {
                supportActionBar?.setTitle(R.string.menu_bill)
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContent, BillFragment())
                    .addToBackStack("BillFragment").commit()
            }
            R.id.menuEmployee -> {
                if (currentUser?.role.equals(ROLE_MANAGER) || currentUser?.role.equals(ROLE_ADMIN)) {
                    supportActionBar?.setTitle(R.string.menu_employee)
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContent, EmployeeFragment())
                        .addToBackStack("EmployeeFragment").commit()
                } else {
                    supportActionBar?.setTitle(R.string.title_access_denied)
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContent, AccessDeniedFragment())
                        .addToBackStack("AccessDeniedFragment").commit()
                }
            }
            R.id.menuEditProfile -> {
                supportActionBar?.setTitle(R.string.menu_edit_profile)
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContent, EditProfileFragment())
                    .addToBackStack("EditProfileFragment").commit()
            }
            R.id.menuLocation -> {
                supportActionBar?.setTitle(R.string.menu_location)
                supportFragmentManager.beginTransaction().replace(R.id.frameLayoutContent, LocationFragment())
                    .addToBackStack("LocationFragment").commit()
            }
            R.id.menuLogOut -> {
                // xóa user hiện tại trong SharedPrefs
                sharedPrefs.clearAll()  // xóa hết luôn cho khỏe
                MyUtil.setSessionUser(null)
                startActivity(Intent(this, LoginActivity::class.java))
                Toast.makeText(this, R.string.toast_login_to_continue, Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateMenu(index: Int) {
        navigationView.menu.getItem(index).setChecked(true)
        onNavigationItemSelected(navigationView.menu.getItem(index))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFragmentManager.popBackStack("TablesFragment", 0)
    }
}

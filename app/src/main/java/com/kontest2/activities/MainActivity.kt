package com.kontest2.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.kontest2.R
import com.kontest2.firebase.FirestoreClass
import com.kontest2.model.User
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider

import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,  ContestItemClicked{

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // API
    private lateinit var mAdapter: ContestListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)

        // This is used to align the xml view to this class
        setContentView(R.layout.activity_main)

        setupActionBar()

        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.
        nav_view.setNavigationItemSelectedListener(this)


        // Get the current logged in user details.
        FirestoreClass().loadUserData(this@MainActivity)


        //
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        // API
        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchData()
        mAdapter = ContestListAdapter(this)
        recyclerView.adapter = mAdapter


    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            // A double back press function is added in Base Activity.
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_my_profile -> {

                // TODO (Step 2: Launch the my profile activity for Result.)
                // START
                startActivityForResult(Intent(this@MainActivity, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
                // END
            }

            R.id.nav_sign_out -> {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }) {
                    // The user is signed in with Google, so revoke access
                    mGoogleSignInClient.revokeAccess()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Successfully revoked access, now sign out from Firebase
                                FirebaseAuth.getInstance().signOut()

                                // Send the user to the intro screen of the application
                                val intent = Intent(this, IntroActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            } else {
                                // Handle the error in revoking access
                                Toast.makeText(this, "Failed to revoke access", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // The user is not signed in with Google, sign out from Firebase only
                    FirebaseAuth.getInstance().signOut()

                    // Send the user to the intro screen of the application
                    val intent = Intent(this, IntroActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // TODO (Step 4: Add the onActivityResult function and check the result of the activity for which we expect the result.)
    // START
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
                && requestCode == MY_PROFILE_REQUEST_CODE
        ) {
            // Get the user updated details.
            FirestoreClass().loadUserData(this@MainActivity)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }
    // END

    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    /**
     * A function for opening and closing the Navigation Drawer.
     */
    private fun toggleDrawer() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    /**
     * A function to get the current user details from firebase.
     */
    fun updateNavigationUserDetails(user: User) {
        // The instance of the header view of the navigation view.
        val headerView = nav_view.getHeaderView(0)

        // The instance of the user image of the navigation view.
        val navUserImage = headerView.findViewById<ImageView>(R.id.iv_user_image)

        // Load the user image in the ImageView.
        Glide
                .with(this@MainActivity)
                .load(user.image) // URL of the image
                .centerCrop() // Scale type of the image.
                .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                .into(navUserImage) // the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        navUsername.text = user.name
    }

    // TODO (Step 1: Create a companion object and a constant variable for My profile Screen result.)
    // START
    /**
     * A companion object to declare the constants.
     */
    companion object {
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }


    //  API
//    private fun fetchData() {
//        showProgressDialog("Loading Contests...")
//        val url = "https://codeforces.com/api/contest.list"
//        val jsonObjectRequest = JsonArrayRequest(
//            Request.Method.GET,
//            url,
//            null,
//            { response ->
//                val contestArray = ArrayList<Contest>()
//                for (i in 0 until response.length()) {
//                    val contestJsonObject = response.getJSONObject(i)
////                    val site_name = contestJsonObject.getString("site")
//                    val cont_name = contestJsonObject.getString("name")
//                    val start_time = contestJsonObject.getString("startTimeSeconds")
////                    val end_time = contestJsonObject.getString("end_time")
////                    val url = contestJsonObject.getString("url")
//                    val contest = Contest(
////                        site_name, cont_name, start_time, end_time, url
//                          cont_name, start_time
//                    )
//                    contestArray.add(contest)
//                }
//
//                mAdapter.updateContest(contestArray)
//                hideProgressDialog()
//            },
//            { error ->
//                // Handle error
//            }
//        )
//        SingletonVolleyInst.getInstance(this).addToRequestQueue(jsonObjectRequest)
//    }


    // Function to convert seconds to a human-readable date and time
    fun convertSecondsToDateTime(seconds: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val date = Date(seconds * 1000) // Convert seconds to milliseconds
        return sdf.format(date)
    }
//    Coder forces
private fun fetchData() {
    showProgressDialog("Loading Contests...")
    val url = "https://codeforces.com/api/contest.list"
    val jsonObjectRequest = JsonObjectRequest(
        Request.Method.GET,
        url,
        null,
        { response ->
            val contestArray = ArrayList<Contest>()
            if (response.has("result")) {
                val result = response.getJSONArray("result")
                for (i in 0 until result.length()) {
                    val contestJsonObject = result.getJSONObject(i)
                    val cont_name = contestJsonObject.getString("name")
                    val start_time = contestJsonObject.getLong("startTimeSeconds")
                    val formattedDateTime = convertSecondsToDateTime(start_time.toLong())
                    val contest = Contest(cont_name, formattedDateTime.toString())
                    contestArray.add(contest)
                }
                mAdapter.updateContest(contestArray)
            }
            hideProgressDialog()
        },
        { error ->
            // Handle error
            hideProgressDialog()
        }
    )
    SingletonVolleyInst.getInstance(this).addToRequestQueue(jsonObjectRequest)
}

    override fun onContClicked(item: Contest) {
        val u = "https://codeforces.com/"
        val builder =  CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
//        customTabsIntent.launchUrl(this, Uri.parse(item.url))
        customTabsIntent.launchUrl(this, Uri.parse(u))
    }


}
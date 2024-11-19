package com.kontest2.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.kontest2.R
import kotlinx.android.synthetic.main.activity_intro.*
import android.widget.Toast


import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.dialog_progress.*

class IntroActivity : AppCompatActivity(){

    /**
     * This function is auto created by Android when the Activity Class is created.
     */

    // Google
    private lateinit var auth: FirebaseAuth
//    private val RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_intro)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // This is used to get the file from the assets folder and set it to the title textView.
        val typeface: Typeface =
            Typeface.createFromAsset(assets, "carbon bl.ttf")
        tv_app_name_intro.typeface = typeface

        btn_sign_in_intro.setOnClickListener {

            // Launch the sign in screen.
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }

        btn_sign_up_intro.setOnClickListener {

            // Launch the sign up screen.
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

            googleSignInBtn.setOnClickListener {
                showProgressDialog("Signing with Google...")
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
        }

    }



    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    hideProgressDialog()
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {

                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Redirect to another activity
                    if (user != null) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("email", user.email)
                        intent.putExtra("name", user.displayName)
                        intent.putExtra("image", user.photoUrl.toString())
                        startActivity(intent)

                        // Store user information in Firestore
                        val db = FirebaseFirestore.getInstance()
                        val userData = hashMapOf(
                            "email" to user.email,
                            "name" to user.displayName,
                            "image" to user.photoUrl.toString()
                        )
                        db.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Handle success
                            }
                            .addOnFailureListener { e ->
                                // Handle errors
                            }
                    }
                } else {
                    hideProgressDialog()
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()

                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var mProgressDialog: Dialog
    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.tv_progress_text.text = text

        //Start the dialog and display it on screen.
        mProgressDialog.show()
    }

    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

}
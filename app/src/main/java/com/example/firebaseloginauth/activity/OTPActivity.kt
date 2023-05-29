package com.example.firebaseloginauth.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.firebaseloginauth.R
import com.example.firebaseloginauth.databinding.ActivityOtpactivityBinding
import com.example.firebaseloginauth.fragment.OTPFragment
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit


class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var number : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneProgressBar.visibility = View.GONE
        firebaseAuth = FirebaseAuth.getInstance()
        binding.sendOTPBtn.setOnClickListener {
            performOTPLogin()
        }
    }

    private fun performOTPLogin() {
        number = binding.phoneEditTextNumber.text.trim().toString()
        if (number.isNotEmpty()) {
            if (number.length == 10) {
                number = "+92$number"
                binding.phoneProgressBar.visibility = View.VISIBLE

                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(number)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)

            } else {
                Toast.makeText(this, "Please Enter correct Number", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please Enter Number", Toast.LENGTH_SHORT).show()

        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
            binding.phoneProgressBar.visibility = View.GONE
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            binding.phoneProgressBar.visibility = View.GONE

            val bundle = Bundle()
            bundle.putString("OTP" , verificationId)
            bundle.putParcelable("resendToken" , token)
            bundle.putString("phoneNumber" , number)
            val otpFragment = OTPFragment()
            otpFragment.arguments = bundle

            supportFragmentManager.beginTransaction().replace(R.id.mainContainer, otpFragment, otpFragment.tag).commit()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this , "Authenticate Successfully" , Toast.LENGTH_SHORT).show()
                    sendToMain()

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun sendToMain(){
        startActivity(Intent(this , MainActivity::class.java))
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        val replace = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(binding.mainContainer.id, fragment, tag)
//        if (addToBackStack) {
//            replace.addToBackStack(null)
//        }
        replace.commit()
    }
}
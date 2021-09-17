package com.example.pagerchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var countrycode:String
    private lateinit var phonenumber:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        phoneNumberEt.addTextChangedListener {
            nextBtn.isEnabled=!(it.isNullOrEmpty() || it.length<10)
        }
        nextBtn.setOnClickListener{
            checkNumnber()
        }


    }

    private fun checkNumnber() {
        countrycode=ccp.selectedCountryCodeWithPlus
        phonenumber=countrycode+phoneNumberEt.text.toString()

        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("We will be verifying the phone number:$phonenumber\n" +
                    "Is this OK, or would you like to edit the number?")

            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }
            setNegativeButton("Edit") { dialog,_ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()


        }
    }

    private fun showLoginActivity() {
        startActivity(Intent(this,OtpActivity::class.java).putExtra(PHONE_NUMBER,phonenumber))
        finish()
    }
}
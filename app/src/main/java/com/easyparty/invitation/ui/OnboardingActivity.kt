package com.easyparty.invitation.ui

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.codemybrainsout.onboarder.AhoyOnboarderActivity
import com.codemybrainsout.onboarder.AhoyOnboarderCard
import com.easyparty.invitation.R
import com.easyparty.invitation.ui.auth.LoginActivity
import java.util.*

class OnboardingActivity : AhoyOnboarderActivity() {
    override fun onFinishButtonPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("from","home" )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ahoyOnboarderCard1 = AhoyOnboarderCard(getString(R.string.welcome),""
            //getString(R.string.descrpition_home)
            , R.drawable.ic_birthday_card)
        ahoyOnboarderCard1.setBackgroundColor(R.color.black_transparent)
        ahoyOnboarderCard1.setTitleColor(R.color.white)
        ahoyOnboarderCard1.setDescriptionColor(R.color.grey_200)
        ahoyOnboarderCard1.setTitleTextSize(dpToPixels(10, this))
        ahoyOnboarderCard1.setDescriptionTextSize(dpToPixels(8, this))
        ahoyOnboarderCard1.setIconLayoutParams(400, 400, 16, 16, 16, 16)

        val ahoyOnboarderCard2 = AhoyOnboarderCard(getString(R.string.welcome1),""
            //getString(R.string.descrpition_gift)
            , R.drawable.gift)
        ahoyOnboarderCard2.setBackgroundColor(R.color.black_transparent)
        ahoyOnboarderCard2.setTitleColor(R.color.white)
        ahoyOnboarderCard2.setDescriptionColor(R.color.grey_200)
        ahoyOnboarderCard2.setTitleTextSize(dpToPixels(10, this))
        ahoyOnboarderCard2.setDescriptionTextSize(dpToPixels(8, this))
        ahoyOnboarderCard2.setIconLayoutParams(400, 400, 16, 16, 16, 16)

        val ahoyOnboarderCard3 = AhoyOnboarderCard(getString(R.string.welcome2),""
            //getString(R.string.descrpition_todo)
            , R.drawable.todo)
        ahoyOnboarderCard3.setBackgroundColor(R.color.black_transparent)
        ahoyOnboarderCard3.setTitleColor(R.color.white)
        ahoyOnboarderCard3.setDescriptionColor(R.color.grey_200)
        ahoyOnboarderCard3.setTitleTextSize(dpToPixels(10, this))
        ahoyOnboarderCard3.setDescriptionTextSize(dpToPixels(8, this))
        ahoyOnboarderCard3.setIconLayoutParams(400, 400, 16, 16, 16, 16)

        val  pages = ArrayList<AhoyOnboarderCard>()
        pages.add(ahoyOnboarderCard1)
        pages.add(ahoyOnboarderCard2)
        pages.add(ahoyOnboarderCard3)
        setFinishButtonTitle(getString(R.string.finish_text))
        setOnboardPages(pages)
        setGradientBackground()

        val lparams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val view = TextView(this)
        view.gravity = Gravity.CENTER
        view.layoutParams = lparams
        lparams.setMargins(0, 0, 0, 0)
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
        view.text = getString(R.string.termsandc)
        view.layoutParams = LinearLayout.LayoutParams(0, 0)
        (findViewById<View>(com.codemybrainsout.onboarder.R.id.buttons_layout) as FrameLayout).addView(view)
    }



}

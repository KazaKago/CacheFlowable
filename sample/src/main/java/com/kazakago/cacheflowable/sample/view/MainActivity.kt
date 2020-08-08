package com.kazakago.cacheflowable.sample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kazakago.cacheflowable.sample.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reposButton.setOnClickListener {
            val intent = GithubReposActivity.createIntent(this, userNameInputEditText.text.toString())
            startActivity(intent)
        }
        userButton.setOnClickListener {
            val intent = GithubUserActivity.createIntent(this, userNameInputEditText.text.toString())
            startActivity(intent)
        }
    }

}
package com.shujinko.app.util

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.shujinko.app.R

fun provideGoogleSignInOptions(context: Context): GoogleSignInOptions =
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

package com.shujinko.app.util

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.shujinko.app.R

fun provideGoogleSignInOptions(context: Context): GoogleSignInOptions =
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestServerAuthCode(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .requestScopes(
            Scope("https://www.googleapis.com/auth/calendar.readonly"),
            Scope("https://www.googleapis.com/auth/user.birthday.read")
        )
        .build()    

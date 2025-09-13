package com.app.dockeep.ui.screens.lock

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun LockScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalActivity.current as FragmentActivity
    val executor = ContextCompat.getMainExecutor(context)

    val biometricPrompt = remember {
        BiometricPrompt(
            context,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // onAuthFailed()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    //biometricPrompt.authenticate(promptInfo)
                }
            })
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Files")
            .setSubtitle("Authenticate to access secure files")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    LaunchedEffect(Unit) {
        biometricPrompt.authenticate(promptInfo)
    }

    Scaffold {
        Box(modifier=Modifier.padding(it)) {

            Column(
                modifier=Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Unlock Dockeep", style = MaterialTheme.typography.headlineMedium)
                Button(
                    {
                        biometricPrompt.authenticate(promptInfo)
                    }
                ) {
                    Text("Use biometrics")
                }
            }
        }
    }
}
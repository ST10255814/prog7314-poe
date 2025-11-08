package com.example.rentwise.auth

import com.example.rentwise.retrofit_instance.RetrofitInstance
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.R
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.GoogleRequest
import com.example.rentwise.data_classes.GoogleResponse
import com.example.rentwise.data_classes.LoginRequest
import com.example.rentwise.data_classes.LoginResponse
import com.example.rentwise.databinding.ActivityLoginBinding
import com.example.rentwise.shared_pref_config.TokenManger
import com.example.rentwise.utils.LocaleHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.common.SignInButton
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import android.util.Base64
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher

// Activity responsible for handling user login, including email/password and Google SSO, with UI feedback and secure token storage.
class LoginActivity : AppCompatActivity() {

    // Holds the binding instance for accessing all views in the login layout.
    private lateinit var binding: ActivityLoginBinding
    // Manages Google Sign-In client for SSO authentication.
    private lateinit var googleSignInClient: GoogleSignInClient
    // Handles secure storage and retrieval of authentication tokens and user data.
    private lateinit var tokenManger: TokenManger
    // Request code for Google Sign-In intent result handling.
    private val RC_SIGN_IN = 9001
    private val KEY_NAME = "biometric_key"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt

    // Biometric encryption state
    private var pendingEncryptForSave = false
    private var pendingDecryptAndRestore = false
    private var tokenToEncrypt: String? = null

    // OVERRIDE ATTACHBASECONTEXT TO APPLY SAVED LOCALE
    // This ensures the saved language is applied when the activity is created
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customizeGoogleButton() //Ensure label is centered and size is wide.

        tokenManger = TokenManger(applicationContext)

        // Only initialize biometrics if available and at least one biometric is enrolled.
        if (isBiometricAvailableAndEnrolled()) {
            initBiometricPrompt()
            try {
                createBiometricKey()
            } catch (e: Exception) {
                // If key creation fails for any reason, log and continue without biometric
                Log.e("Biometric", "Failed to create biometric key: ${e.message}", e)
                // Hide biometric UI to avoid user confusion
                binding.biometricContainer.visibility = View.GONE
            }
        } else {
            // Hide the fingerprint animation if biometric auth isn't available/enrolled
            binding.biometricContainer.visibility = View.GONE
        }

        setupLoginView() // Styles the app name, slogan, and register text for branding and navigation cues.
        prepareGoogleSignIn() // Configures Google Sign-In options and initializes the client.
        setListeners() // Attaches all event listeners for user interaction, input validation, and authentication triggers.

        // Update fingerprint visibility after onCreate finishes
        updateFingerprintVisibility()
    }

    // Add onResume to update fingerprint visibility
    override fun onResume() {
        super.onResume()
        updateFingerprintVisibility()
    }

    //  Returns true if device supports biometrics AND at least one biometric is enrolled.
    //  If it detects no enrollment, it shows a dialog offering to open the system enroll screen.
    //  Logs the detailed BiometricManager response for easier debugging.
    private fun isBiometricAvailableAndEnrolled(): Boolean {
        try {
            val biometricManager = BiometricManager.from(this)

            // Try to check for strong biometric OR device credential as fallback
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL

            val canAuth = biometricManager.canAuthenticate(authenticators)
            when (canAuth) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    Log.d("BiometricCheck", "BIOMETRIC_SUCCESS: hardware present and enrolled")
                    return true
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    Log.w("BiometricCheck", "BIOMETRIC_ERROR_NO_HARDWARE: No biometric hardware available")
                    return false
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    Log.w("BiometricCheck", "BIOMETRIC_ERROR_HW_UNAVAILABLE: Biometric hardware unavailable (busy)")
                    return false
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Log.i("BiometricCheck", "BIOMETRIC_ERROR_NONE_ENROLLED: Hardware present but no biometrics enrolled")
                    // Offer to open enroll screen
                    showEnrollDialog()
                    return false
                }
                else -> {
                    Log.w("BiometricCheck", "canAuthenticate returned code=$canAuth")
                    return false
                }
            }
        } catch (e: Exception) {
            Log.e("BiometricCheck", "Error checking biometric availability: ${e.message}", e)
            return false
        }
    }

    // Prompt user with dialog and an action to open system biometric enrollment screen.
    private fun showEnrollDialog() {
        try {
            AlertDialog.Builder(this)
                .setTitle("Enable biometrics")
                .setMessage("Biometric hardware is available but no biometric credential is enrolled. Would you like to enroll now?")
                .setPositiveButton("Enroll") { _, _ -> promptEnroll() }
                .setNegativeButton("Not now", null)
                .show()
        } catch (e: Exception) {
            Log.e("BiometricEnrollDialog", "Failed to show enroll dialog: ${e.message}", e)
        }
    }

    // Opens the system biometric enrollment screen with proper API level handling
    private fun promptEnroll() {
        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    // API 30+ - Use modern biometric enrollment
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                    }
                    startActivity(enrollIntent)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    // API 28-29 - Use fingerprint enrollment
                    @Suppress("DEPRECATION")
                    val fpIntent = Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                    if (fpIntent.resolveActivity(packageManager) != null) {
                        startActivity(fpIntent)
                    } else {
                        startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    }
                }
                else -> {
                    // API 26-27 - Fall back to security settings
                    startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }
            }
        } catch (e: Exception) {
            Log.e("BiometricEnroll", "Failed to open enroll screen: ${e.message}", e)
            // Final fallback to security settings
            try {
                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            } catch (_: Exception) {
                CustomToast.show(this, "Unable to open biometric enrollment. Please set up biometrics manually in Settings.", CustomToast.Companion.ToastType.ERROR)
            }
        }
    }

    // Configures Google Sign-In with required scopes and initializes the client for SSO.
    private fun prepareGoogleSignIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    // Styles the app name, slogan, and register text, applying color and underline to specific portions for visual emphasis.
    // Spannable text implementation by:
    //Programmer World. 2023. How to edit the text in TextView using spannable string in your Android App?. [video online]
    //Available at: <https://youtu.be/UR-oQynC12E?si=_2Lvcr7al9a4wgov> [Accessed 5 August 2025].
    private fun setupLoginView(){
        val slogan = getString(R.string.app_slogan)
        val smartPortionOfSlogan = "Smart".length
        val simplePortionOfSlogan = "Simple".length

        val firstHalfOfSlogan = "Smart Rentals. ".length

        val registerText = getString(R.string.register_text)
        val registerPortion = "Register".length

        val color = ContextCompat.getColor(this, R.color.darkish_blue)

        val spannableSlogan = SpannableString(slogan)

        spannableSlogan.setSpan(
            ForegroundColorSpan(color),
            0,
            smartPortionOfSlogan,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableSlogan.setSpan(
            ForegroundColorSpan(color),
            firstHalfOfSlogan,
            firstHalfOfSlogan + simplePortionOfSlogan,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val spannableRegister = SpannableString(registerText)
        spannableRegister.setSpan(
            UnderlineSpan(),
            registerText.length - registerPortion,
            registerText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableRegister.setSpan(
            ForegroundColorSpan(color),
            registerText.length - registerPortion,
            registerText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.appSlogan.text = spannableSlogan
        binding.registerText.text = spannableRegister
    }

    @SuppressLint("ClickableViewAccessibility")
    // Sets up all listeners for UI elements, including navigation, button animations, input validation, and authentication triggers.
    private fun setListeners(){
        // Navigates to the registration screen when the register text is clicked.
        binding.registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Validates user input and triggers the login API call when the login button is clicked.
        binding.loginBtn.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            // Checks for empty fields and valid email format before proceeding.
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isEmpty()) {
                binding.emailLayout.error = "Email can not be empty"
                binding.passwordLayout.error = "Password can not be empty"
            }
            else{
                loginAPICall(email, password) // Initiates the login process via API.
            }
        }

        // Clears the email error message as soon as the user starts typing.
        binding.edtEmail.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                binding.emailLayout.error = null
            }
        }

        // Clears the password error message as soon as the user starts typing.
        binding.edtPassword.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                binding.passwordLayout.error = null
            }
        }

        // Placeholder for biometric authentication trigger (to be implemented).
        binding.fingerprintAnimation.setOnClickListener {
            startBiometricRestoreFlow()
        }

        //OpenAI. 2025. In android studio kotlin i want to animate the button and make it a different color when hovered/clicked on. How can i do this?.
        //[ChatGPT]. Available at: <https://chatgpt.com/share/689214fa-941c-800a-a9d7-81bfe8fefbf1> [Accessed 5 August 2025]
        // Adds a press animation to the login button for tactile feedback.
        binding.loginBtn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }

        // Adds a press animation to the Google Sign-In button for tactile feedback.
        binding.googleSignInBtn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }

        // Adds a press animation to the register text for tactile feedback.
        binding.registerText.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }

        // Handles Google Sign-In button click, ensuring account selection and launching the sign-in intent.
        binding.googleSignInBtn.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    // Handles the login API call, manages UI overlays, and processes server responses for success or error.
    private fun loginAPICall(email: String, password: String){
        showLoginOverlay()
        val request = LoginRequest(
            email = email,
            password = password
        )

        val api = RetrofitInstance.createAPIInstance(applicationContext)
        api.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                hideLoginOverlay()
                if(response.isSuccessful) {
                    val authResponse = response.body()
                    if(authResponse != null){
                        val data = authResponse.data
                        if (data != null) {
                            val token = data.token ?: ""
                            val userIdRaw = data.userId
                            val userIdStr = try {
                                userIdRaw?.toString() ?: ""
                            } catch (_: Exception) {
                                ""
                            }
                            val pfpStr = ""

                            // Persist token, userId and pfp atomically so HomeScreen sees them immediately
                            tokenManger.saveAllSync(
                                token.takeIf { it.isNotEmpty() },
                                userIdStr.takeIf { it.isNotEmpty() },
                                pfpStr.takeIf { it.isNotEmpty() }
                            )

                            if (token.isNotEmpty()) {
                                // Show biometric dialog but also ensure user can proceed without it
                                showEnableBiometricDialog(token, userIdStr.takeIf { it.isNotEmpty() }, pfpStr.takeIf { it.isNotEmpty() })
                            } else {
                                CustomToast.show(this@LoginActivity, "${authResponse.message}", CustomToast.Companion.ToastType.SUCCESS)
                                proceedToHome()
                            }
                        } else {
                            CustomToast.show(this@LoginActivity, "Invalid server response", CustomToast.Companion.ToastType.ERROR)
                            proceedToHome() // Still proceed to avoid blocking user
                        }
                    } else {
                        CustomToast.show(this@LoginActivity, "Empty response body", CustomToast.Companion.ToastType.ERROR)
                        proceedToHome() // Still proceed to avoid blocking user
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            e.message.toString()
                        }
                    } else {
                        "Unknown error"
                    }
                    binding.emailLayout.error = "Invalid Credentials"
                    binding.passwordLayout.error = "Invalid Credentials"
                    CustomToast.show(this@LoginActivity, errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable){
                hideLoginOverlay()
                CustomToast.show(this@LoginActivity, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                proceedToHome() // Proceed even on network failure to avoid blocking user
            }
        })
    }

    // Makes the login overlay visible to block user interaction during network operations.
    private fun showLoginOverlay(){
        binding.loginOverlay.visibility = View.VISIBLE
    }

    // Hides the login overlay, restoring user interaction after network operations.
    private fun hideLoginOverlay(){
        binding.loginOverlay.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    // Handles the result from the Google Sign-In intent, extracting the ID token and sending it to the backend for authentication.
    //Developer. 2025. Integrate Google Sign-In into Your Android App. Developers. [online]
    //Available at: <https://developer.android.com/identity/legacy/gsi/legacy-sign-in> [Accessed 17 September 2025].
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002) {
            // User returned from enroll/settings — re-check biometrics and initialize if available
            if (isBiometricAvailableAndEnrolled()) {
                try {
                    initBiometricPrompt()
                    createBiometricKey()
                    binding.fingerprintAnimation.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e("BiometricPostEnroll", "Failed to init key after enroll: ${e.message}", e)
                    binding.fingerprintAnimation.visibility = View.GONE
                }
            } else {
                binding.fingerprintAnimation.visibility = View.GONE
            }
            // NEW: Update visibility after enrollment check
            updateFingerprintVisibility()
        } else if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    Log.d("Google Token", idToken)
                    sendIdTokenToBackend(idToken)
                } else {
                    CustomToast.show(this@LoginActivity, "Failed to get Google ID Token", CustomToast.Companion.ToastType.ERROR)
                }
            } catch (e: ApiException) {
                Log.e("Google Token Error", "${e.statusCode}", e)
                CustomToast.show(this@LoginActivity, "Google sign-in failed", CustomToast.Companion.ToastType.ERROR)
            }
        }
    }

    // Sends the Google ID token to the backend for verification and handles the authentication response.
    private fun sendIdTokenToBackend(idToken: String) {
        showLoginOverlay()
        val request = GoogleRequest(
            idToken = idToken
        )
        val api = RetrofitInstance.createAPIInstance(applicationContext)
        api.googleMobileLogin(request).enqueue(object : Callback<GoogleResponse> {
            override fun onResponse(
                call: Call<GoogleResponse?>,
                response: Response<GoogleResponse?>
            ) {
                hideLoginOverlay()
                if(response.isSuccessful){
                    val googleResponse = response.body()
                    // Saves user ID, JWT token, and profile image for session and personalization.
                    if(googleResponse != null){
                        val token = googleResponse.token ?: ""
                        // user may be an object with id/pfp fields
                        val userObj = googleResponse.user
                        val userIdStr = try { userObj?.id?.toString() ?: "" } catch (_: Exception) { "" }
                        val pfpStr = try { userObj?.pfpImage?.toString() ?: "" } catch (_: Exception) { "" }

                        // Persist token, userId and pfp atomically so HomeScreen sees them immediately
                        tokenManger.saveAllSync(
                            token.takeIf { it.isNotEmpty() },
                            userIdStr.takeIf { it.isNotEmpty() },
                            pfpStr.takeIf { it.isNotEmpty() }
                        )

                        if (token.isNotEmpty()) {
                            // NEW: Ask user for consent to enable biometric quick-signin
                            showEnableBiometricDialog(token, userIdStr.takeIf { it.isNotEmpty() }, pfpStr.takeIf { it.isNotEmpty() })
                            return
                        } else {
                            // token missing -> show success message then proceed
                            CustomToast.show(this@LoginActivity, "Login Successful!", CustomToast.Companion.ToastType.SUCCESS)
                            proceedToHome()
                            return
                        }
                    } else {
                        CustomToast.show(this@LoginActivity, "Empty response body", CustomToast.Companion.ToastType.ERROR)
                        return
                    }
                } else {
                    // Extracts and displays error messages from the server response.
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            e.message.toString()
                        }
                    } else {
                        "Unknown error"
                    }
                    CustomToast.show(this@LoginActivity, errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            // Handles network or unexpected failures during the Google login process.
            override fun onFailure(
                call: Call<GoogleResponse?>,
                t: Throwable
            ) {
                hideLoginOverlay()
                CustomToast.show(this@LoginActivity, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Login", "Error: ${t.message.toString()}")
            }
        })
    }

    //Centers the Google button’s label while keeping your existing SignInButton.
    //Also enforces the official wide size for consistent spacing.
    private fun customizeGoogleButton() {
        // Use Google's wide layout for better label centering and spacing.
        binding.googleSignInBtn.setSize(SignInButton.SIZE_WIDE)

        // Center the inner text view so the label appears centered regardless of the 'G' icon.
        for (i in 0 until binding.googleSignInBtn.childCount) {
            val child = binding.googleSignInBtn.getChildAt(i)
            if (child is TextView) {
                child.gravity = Gravity.CENTER
                child.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                child.layoutParams = (child.layoutParams).apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                }
                // Optional: keep your own string resource label if present.
                try {
                    child.text = getString(R.string.sign_in_with_google)
                } catch (_: Exception) {}
                break
            }
        }
    }

    //Reference https://developer.android.com/identity/sign-in/biometric-auth#kotlin (To be implemented properly in part 3)
    //Debug assistance from OpenAI https://chatgpt.com/share/68cb8835-e174-8012-b4c1-3f3122ac3f57
    private fun initBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val crypto = result.cryptoObject?.cipher
                try {
                    if (pendingEncryptForSave) {
                        // encrypt tokenToEncrypt and store ciphertext + iv
                        val token = tokenToEncrypt ?: ""
                        val encrypted = crypto?.doFinal(token.toByteArray(Charsets.UTF_8))
                        val iv = crypto?.iv
                        if (encrypted != null && iv != null) {
                            val ctBase64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
                            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
                            // Persist encrypted payload synchronously so it's definitely stored before navigation
                            tokenManger.saveEncryptedToken(ctBase64, ivBase64, commit = true)
                            CustomToast.show(this@LoginActivity, "Biometric enabled for quick sign-in", CustomToast.Companion.ToastType.SUCCESS)
                            // NEW: Update fingerprint visibility after saving
                            updateFingerprintVisibility()
                            // NEW: After successfully enabling biometrics, proceed to home (we waited so biometric prompt isn't dismissed)
                            proceedToHome()
                        } else {
                            Log.e("BiometricSave", "crypto returned null during encrypt")
                            // Still proceed to home so user isn't stuck
                            proceedToHome()
                        }
                        pendingEncryptForSave = false
                        tokenToEncrypt = null
                    } else if (pendingDecryptAndRestore) {
                        // decrypt stored ciphertext and restore token
                        val pair = tokenManger.getEncryptedTokenPair()
                        if (pair != null) {
                            val (ctBase64, ivBase64) = pair
                            val ciphertext = Base64.decode(ctBase64, Base64.NO_WRAP)
                            // crypto should already be initialized for DECRYPT_MODE with IV you used; do final to decrypt
                            val plain = crypto?.doFinal(ciphertext)
                            if (plain != null) {
                                val payload = String(plain, Charsets.UTF_8)
                                val (token, userId, pfp) = tokenManger.parseBiometricPayload(payload)

                                // Save all data atomically so HomeScreen sees all fields immediately
                                tokenManger.saveAllSync(token, userId, pfp) // <-- USE synchronous atomic save

                                Log.d("BiometricRestore", "Restored credentials: userId=$userId, tokenPresent=${token != null}, pfpPresent=${pfp != null}")

                                CustomToast.show(this@LoginActivity, "Login Successful!", CustomToast.Companion.ToastType.SUCCESS)
                                proceedToHome()
                            } else {
                                CustomToast.show(this@LoginActivity, "Failed to restore session", CustomToast.Companion.ToastType.ERROR)
                            }
                        } else {
                            CustomToast.show(this@LoginActivity, "No biometric-saved credentials found", CustomToast.Companion.ToastType.ERROR)
                        }
                        pendingDecryptAndRestore = false
                    } else {
                        // fallback: your previous behavior (check tokenManger.getToken(), etc.)
                        val jwt = tokenManger.getToken()
                        val userId = tokenManger.getUser()
                        if (!jwt.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                            CustomToast.show(this@LoginActivity, "Login Successful!", CustomToast.Companion.ToastType.SUCCESS)
                            Log.d("BiometricJWT", "JWT=$jwt, userId=$userId")
                            proceedToHome()
                        } else {
                            CustomToast.show(this@LoginActivity, "No active session. Please login manually", CustomToast.Companion.ToastType.ERROR)
                        }
                    }
                } catch (e: BadPaddingException) {
                    Log.e("Biometric", "BadPaddingException during decryption: ${e.message}", e)
                    handleCryptoError("Decryption failed - data may be corrupted")
                } catch (e: IllegalBlockSizeException) {
                    Log.e("Biometric", "IllegalBlockSizeException during decryption: ${e.message}", e)
                    handleCryptoError("Decryption failed - data format error")
                } catch (e: InvalidKeyException) {
                    Log.e("Biometric", "InvalidKeyException during decryption: ${e.message}", e)
                    handleCryptoError("Key error - please re-enable biometric login")
                    // Clear the corrupted key and encrypted data
                    tokenManger.clearEncryptedToken()
                    updateFingerprintVisibility()
                } catch (e: Exception) {
                    Log.e("Biometric", "onAuthSucceeded error: ${e.message}", e)
                    handleCryptoError("Biometric operation failed")
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                CustomToast.show(this@LoginActivity, "Authentication error: $errString", CustomToast.Companion.ToastType.ERROR)
                if (pendingEncryptForSave) {
                    pendingEncryptForSave = false
                    tokenToEncrypt = null
                    proceedToHome()
                } else {
                    pendingEncryptForSave = false
                    pendingDecryptAndRestore = false
                    tokenToEncrypt = null
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                CustomToast.show(this@LoginActivity, "Authentication failed", CustomToast.Companion.ToastType.ERROR)
                if (pendingEncryptForSave) {
                    pendingEncryptForSave = false
                    tokenToEncrypt = null
                    proceedToHome()
                }
            }
        })
    }

    private fun handleCryptoError(message: String) {
        CustomToast.show(this@LoginActivity, message, CustomToast.Companion.ToastType.ERROR)
        pendingEncryptForSave = false
        pendingDecryptAndRestore = false
        tokenToEncrypt = null

        // Clear corrupted biometric data
        tokenManger.clearEncryptedToken()
        updateFingerprintVisibility()
    }

    private fun createBiometricKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            // Check if key already exists
            if (keyStore.containsAlias(KEY_NAME)) {
                try {
                    // Try to get the key to see if it's valid
                    val key = keyStore.getKey(KEY_NAME, null) as? SecretKey
                    if (key != null) {
                        Log.d("BiometricKey", "Existing key found and valid")
                        return
                    }
                } catch (e: Exception) {
                    Log.w("BiometricKey", "Existing key invalid, will recreate: ${e.message}")
                }
                // Remove invalid key
                keyStore.deleteEntry(KEY_NAME)
            }

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keySpecBuilder = KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true) // Key invalidated when biometrics change

            // Add user authentication validity for better UX - require auth every time for security
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                keySpecBuilder.setUserAuthenticationParameters(
                    0, // timeout in seconds (0 means auth required every use)
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
            } else {
                @Suppress("DEPRECATION")
                keySpecBuilder.setUserAuthenticationValidityDurationSeconds(-1) // require auth every use
            }

            val keySpec = keySpecBuilder.build()
            keyGenerator.init(keySpec)
            keyGenerator.generateKey()
            Log.d("BiometricKey", "New key generated successfully")
        } catch (e: Exception) {
            // Propagate the exception to caller or handle locally; here we rethrow to be caught in onCreate
            Log.e("BiometricKey", "Error generating key: ${e.message}", e)
            throw e
        }
    }

    private fun getCipher(): Cipher = Cipher.getInstance("AES/GCM/NoPadding")

    private fun getSecretKey(): SecretKey {
        val keyStore = java.security.KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEY_NAME, null) as SecretKey
    }


    // Biometric encryption flows

    private fun startBiometricSaveTokenFlow(payloadJson: String) {
        try {
            // prepare cipher for encryption
            val cipher = getCipher()
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            tokenToEncrypt = payloadJson
            pendingEncryptForSave = true

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm biometrics to enable quick sign-in")
                .setSubtitle("Use your fingerprint to secure quick sign-in")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: InvalidKeyException) {
            Log.e("BiometricSave", "InvalidKeyException: ${e.message}", e)
            // Key might be invalidated, try to recreate
            try {
                createBiometricKey()
                val cipher = getCipher()
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
                tokenToEncrypt = payloadJson
                pendingEncryptForSave = true

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Confirm biometrics to enable quick sign-in")
                    .setSubtitle("Use your fingerprint to secure quick sign-in")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            } catch (e2: Exception) {
                Log.e("BiometricSave", "Failed to recreate key: ${e2.message}", e2)
                CustomToast.show(this@LoginActivity, "Unable to enable biometric sign-in", CustomToast.Companion.ToastType.ERROR)
                pendingEncryptForSave = false
                tokenToEncrypt = null
                proceedToHome()
            }
        } catch (e: Exception) {
            Log.e("BiometricSave", "Failed to start biometric save flow: ${e.message}", e)
            CustomToast.show(this@LoginActivity, "Unable to enable biometric sign-in", CustomToast.Companion.ToastType.ERROR)
            // If enabling biometrics failed to start, allow user to continue to the app
            pendingEncryptForSave = false
            tokenToEncrypt = null
            proceedToHome()
        }
    }

    private fun startBiometricRestoreFlow() {
        val pair = tokenManger.getEncryptedTokenPair()
        if (pair == null) {
            CustomToast.show(this@LoginActivity, "No biometric-saved credentials found. Please login manually", CustomToast.Companion.ToastType.ERROR)
            return
        }
        try {
            val (_, ivBase64) = pair
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val cipher = getCipher()
            val spec = javax.crypto.spec.GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            pendingDecryptAndRestore = true

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock with biometrics")
                .setSubtitle("Use your fingerprint to sign in")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: InvalidKeyException) {
            Log.e("BiometricRestore", "InvalidKeyException: ${e.message}")
            CustomToast.show(this@LoginActivity, "Biometric key error - please login again", CustomToast.Companion.ToastType.ERROR)
            // Clear corrupted data
            tokenManger.clearEncryptedToken()
            updateFingerprintVisibility()
        } catch (e: Exception) {
            Log.e("BiometricRestore", "Failed to start restore: ${e.message}", e)
            CustomToast.show(this@LoginActivity, "Unable to use biometric sign-in", CustomToast.Companion.ToastType.ERROR)
        }
    }

    // Biometric consent dialog and visibility helpers

    private fun showEnableBiometricDialog(token: String, userId: String?, pfp: String?) {
        try {
            // Only offer if hardware/enrolled and no biometric credential exists
            if (!isBiometricAvailableAndEnrolled() || tokenManger.getEncryptedTokenPair() != null) {
                proceedToHome()
                return
            }

            AlertDialog.Builder(this)
                .setTitle("Enable biometric sign-in")
                .setMessage("Would you like to enable quick sign-in using your fingerprint or device PIN?")
                .setPositiveButton("Yes") { _, _ ->
                    val payload = tokenManger.createBiometricPayload(token, userId, pfp)

                    // Mask token for log (do NOT log full JWT)
                    val maskedPayloadForLog = try {
                        val json = JSONObject(payload)
                        val maskToken = json.optString("token")?.let {
                            if (it.length > 8) it.take(4) + "… " + it.takeLast(4) else "masked"
                        } ?: "no-token"
                        "payload userId=${json.optString("userId", "null")}, token_preview=$maskToken"
                    } catch (e: Exception) {
                        "payload-parsing-failed"
                    }
                    Log.d("BiometricPayload", maskedPayloadForLog)

                    startBiometricSaveTokenFlow(payload)
                }
                .setNegativeButton("No") { _, _ ->
                    proceedToHome()
                }
                .setOnCancelListener {
                    proceedToHome()
                }
                .show()

        } catch (e: Exception) {
            Log.e("BiometricDialog", "Failed to show enable dialog: ${e.message}", e)
            proceedToHome()
        }
    }

    //Update FingerprintVisibility to validate token
    private fun updateFingerprintVisibility() {
        val pair = tokenManger.getEncryptedTokenPair()
        if (pair == null) {
            binding.fingerprintAnimation.visibility = View.GONE
            Log.d("BiometricStatus", "No encrypted payload found")
            return
        }

        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            // Try to retrieve the secret key; this won't prompt biometric auth.
            val keyExists = try {
                val secret = keyStore.getKey(KEY_NAME, null) as? SecretKey
                secret != null
            } catch (e: Exception) {
                Log.w("BiometricStatus", "getKey threw: ${e.message}")
                false
            }

            if (!keyExists) {
                // Encrypted payload exists but the key doesn't — clear payload
                tokenManger.clearEncryptedToken()
                binding.fingerprintAnimation.visibility = View.GONE
                Log.w("BiometricStatus", "Encrypted payload present but key missing/invalid. Cleared stored biometric data.")
                return
            }

            // At this point we have both encrypted data and the key present.
            // Do NOT attempt to decrypt here — show the fingerprint UI and let the biometric prompt handle unlocking.
            binding.fingerprintAnimation.visibility = View.VISIBLE
            Log.d("BiometricStatus", "Encrypted payload + keystore key found: showing fingerprint UI")

        } catch (e: Exception) {
            Log.e("BiometricStatus", "Error checking biometric status: ${e.message}", e)
            binding.fingerprintAnimation.visibility = View.GONE
        }
    }

    // navigate to HomeScreen and finish this activity
    private fun proceedToHome() {
        try {
            val intent = Intent(this@LoginActivity, HomeScreen::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("Navigation", "Failed to navigate to HomeScreen: ${e.message}", e)
        }
    }
}
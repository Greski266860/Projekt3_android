package com.example.projekt3

import android.content.Context
import android.os.Bundle
import android.os.Parcelable

import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.projekt3.ui.theme.Projekt3Theme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.security.MessageDigest
import java.util.UUID


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Projekt3Theme {

                Aplikacja(modifier = Modifier.fillMaxSize(),)
            }
        }
    }
}

@Composable
fun Aktywnosci(navigateToPowitanie: (User) -> Unit, modifier: Modifier = Modifier) {

    var user = User(null)
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val authenticationManager = remember{
        Authentication(context)
    }
    val coroutineScope = rememberCoroutineScope()
    Surface (
        color = MaterialTheme.colorScheme.primary
    ){
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center

        ) {
            Text("Podaj login i hasło")
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { newValue ->
                    email = newValue
                },
                placeholder = {
                    Text("Email")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Email, contentDescription = null)
                },
                shape = RoundedCornerShape(16.dp), modifier = modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { newValue ->
                    password = newValue
                },
                placeholder = {
                    Text("Hasło")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp), modifier = modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                authenticationManager.loginWithEmail(email, password)
                    .onEach { response ->
                        if(response is AuthResponse.Success){
                            user.name = FirebaseAuth.getInstance().currentUser?.email
                            navigateToPowitanie(user)
                        }
                    }
                    .launchIn(coroutineScope)
            },modifier = modifier.fillMaxWidth()) {
                Text("Zaloguj")
            }

            Text(
                text = "lub kontynuuj za pomocą:"

            )

            OutlinedButton(onClick = {
                authenticationManager.signInWithGoogle()
                    .onEach { response ->
                        if(response is AuthResponse.Success){
                            user.name = FirebaseAuth.getInstance().currentUser?.email
                            navigateToPowitanie(user)
                        }
                    }
                    .launchIn(coroutineScope)
            }, modifier = modifier.fillMaxWidth()){

                Image(
                    painter = painterResource(id = com.google.firebase.appcheck.interop.R.drawable.googleg_standard_color_18),
                    contentDescription = null,


                )
            }
        }

    }
}


@Composable
fun EkranStart(navigateToAktywnosci: (Klik) -> Unit, modifier: Modifier = Modifier){
    val klik = Klik("")
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text("Grzegorz Wróblewski 266860, Projekt 3")
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = {
                navigateToAktywnosci(klik)
            }

        ) {
            Text("Przejdź dalej")
        }
    }
}
@Composable
fun Powitanie(user: User,
              modifier: Modifier = Modifier){
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("Witaj:")
        Text("${user.name}")
    }
}

class Authentication(val context: Context) {
    private val auth = Firebase.auth

    fun createAccount(email: String, password: String): Flow<AuthResponse> = callbackFlow{
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    trySend(AuthResponse.Success)
                }else{
                    trySend(AuthResponse.Error(message = task.exception?.message ?: ""))
                }
            }
        awaitClose()
    }

    fun loginWithEmail(email: String, password: String): Flow<AuthResponse> = callbackFlow{
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    trySend(AuthResponse.Success)
                }else{
                    trySend(AuthResponse.Error(message = task.exception?.message ?: ""))
                }
            }
        awaitClose()
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold(""){ str, it ->
            str + "%02x".format(it)
        }

    }

    fun signInWithGoogle(): Flow<AuthResponse> = callbackFlow{
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        try{
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context = context, request = request)

            val credential = result.credential
            if(credential is CustomCredential){
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
                    try{
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val firebaseCredential = GoogleAuthProvider
                            .getCredential(
                                googleIdTokenCredential.idToken,
                                null
                            )

                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener {
                                if(it.isSuccessful){
                                    trySend(AuthResponse.Success)
                                }else{
                                    trySend(AuthResponse.Error(message = it.exception?.message ?: ""))
                                }
                            }
                    }catch (e: GoogleIdTokenParsingException){
                        trySend(AuthResponse.Error(message = e.message ?: ""))
                    }
                }
            }

        }catch (e : Exception){
            trySend(AuthResponse.Error(message = e.message ?: ""))
        }
        awaitClose()
    }
}

interface AuthResponse{
    data object Success: AuthResponse
    data class Error(val message: String): AuthResponse
}


@Parcelize
data class User(var name: String?) : Parcelable
@Parcelize
data class Klik(var name: String?) : Parcelable
@Composable
fun Aplikacja(modifier: Modifier){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "EkranStart"){
            composable("EkranStart"){
                EkranStart(
                    navigateToAktywnosci = {klik ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("klik", klik)
                        navController.navigate("Aktywnosci")
                    }
                )
            }
            composable(route = "Aktywnosci"){

                Aktywnosci(
                    navigateToPowitanie = {user ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("user", user)
                        navController.navigate("Powitanie")

                    }
                )
            }

            composable(route = "Powitanie"){
                val user = navController.previousBackStackEntry?.savedStateHandle?.get<User>("user")
                if(user != null){
                    Powitanie(user)
                }
            }

    }
}


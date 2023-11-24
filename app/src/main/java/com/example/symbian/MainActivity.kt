package com.example.symbian

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.symbian.ui.theme.SymbianTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SymbianTheme {
                LoginScreen(lifecycleScope)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    lifecycleScope: LifecycleCoroutineScope
) {

    var context = LocalContext.current

    // Obter foto da galeria de imagens
    var photoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var emailState by remember {
        mutableStateOf(value = "")
    }

    var passwordState by remember {
        mutableStateOf(value = "")
    }

    // Criar o objeto que abrirá a galeria e retornará
    // a Uri da imagem selecionada
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ){
        photoUri = it
    }

    var painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context).data(photoUri).build()
    )

    //ATRIBUTO PARA ACESSO E MANIPULAÇÃO DO STORAGE
    var storageRef: StorageReference = FirebaseStorage.getInstance().reference.child("images")

    //ATRIBUTO PARA ACESSO E MANIPULAÇÃO DO FIRESTORE DATABASE
    var firebaseFireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    var url by remember {
        mutableStateOf("")
    }

    fun login(
        foto: Uri,
        email: String,
        password: String
    ) {
         val responseRepository = LoginRepository()

        lifecycleScope.launch{

            photoUri?.let {
                storageRef.putFile(it).addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        storageRef.downloadUrl.addOnSuccessListener { uri ->

                            val map = HashMap<String, Any>()
                            map["pic"] = uri.toString()

                            firebaseFireStore.collection("images").add(map)
                                .addOnCompleteListener { firestoreTask ->

                                    if (firestoreTask.isSuccessful) {
                                        url = uri.toString()
                                        Toast.makeText(
                                            context,
                                            "UPLOAD REALIZADO COM SUCESSO",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "ERRO AO TENTAR REALIZAR O UPLOAD",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }

                    } else {
                        Toast.makeText(
                            context,
                            "ERRO AO TENTAR REALIZAR O UPLOAD",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        if (email != "" && password != "" && url != "") {
            lifecycleScope.launch {
                Log.e("teste", "${email} ${password} $url")
                val response = responseRepository.loginUser(email, password, url)
                Log.e("res", "$response")

                if (response.isSuccessful) {
                    Toast.makeText(context, "Login com sucesso!", Toast.LENGTH_LONG).show()
                    val body = response.body()
                } else {
                    Toast.makeText(context, "Seu login deu errado!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Surface (
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(
                modifier = Modifier
                    .size(100.dp)
            ) {
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .align(alignment = Alignment.TopEnd),
                    shape = CircleShape,
                    border = BorderStroke(
                        width = 2.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color.Blue,
                                Color.White
                            )
                        )
                    )
                ) {

                    Image(
                        painter = painter,
                        contentDescription = "",
                        modifier = Modifier
                            .size(180.dp),
                        contentScale = ContentScale.Crop
                    )
                    Log.e("teste", "$painter")

                }
                Image(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = null,
                    modifier = Modifier
                        .size(26.dp)
                        .offset(
                            x = 0.dp,
                            y = 0.dp
                        )
                        .align(alignment = Alignment.BottomEnd)
                        .clickable {
                            launcher.launch("image/*")
                        }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = emailState,
                onValueChange = { emailState = it },
                modifier = Modifier.width(330.dp),
                shape = RoundedCornerShape(16.dp),
                label = { (Text(text = "E-mail")) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = passwordState,
                onValueChange = { passwordState = it },
                modifier = Modifier.width(330.dp),
                shape = RoundedCornerShape(16.dp),
                label = { (Text(text = "Senha")) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    login(foto = photoUri!!, emailState, passwordState)
                },
                Modifier
                    .height(48.dp)
                    .width(140.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(Color(6, 64, 240, 255))
            ) {
                Text(
                    text = "ENTRAR",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
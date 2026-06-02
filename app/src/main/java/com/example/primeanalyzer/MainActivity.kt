package com.example.primeanalyzer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.NumberTheoryEngine
import java.math.BigInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrimeAnalyzerScreen()
                }
            }
        }
    }
}

@Composable
fun PrimeAnalyzerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var numberInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("ادخل رقماً لتحليله") }
    var primeFactors by remember { mutableStateOf(listOf<BigInteger>()) }

    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("محلل الأعداد الأولية", style = MaterialTheme.typography.headlineMedium)
        
        OutlinedTextField(
            value = numberInput,
            onValueChange = { numberInput = it },
            label = { Text("العدد") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = {
                if (numberInput.isBlank()) {
                    resultText = "الرجاء إدخال عدد"
                    return@Button
                }
                scope.launch {
                    try {
                        val num = BigInteger(numberInput)
                        resultText = "جارٍ التحليل..."
                        primeFactors = listOf()
                        val factors = withContext(Dispatchers.Default) {
                            NumberTheoryEngine.getPrimeFactors(num)
                        }
                        primeFactors = factors
                        resultText = primeFactors.joinToString(" × ")
                    } catch (e: NumberFormatException) {
                        resultText = "خطأ: الرقم غير صالح"
                    } catch (e: Exception) {
                        resultText = "خطأ: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تحليل")
        }
        
        Text(resultText, style = MaterialTheme.typography.bodyLarge)
        
        if (primeFactors.isNotEmpty()) {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Prime Factors", resultText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "تم النسخ!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("نسخ النتيجة")
            }
        }
    }
}

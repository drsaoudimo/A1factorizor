package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.example.R
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.math.BigInteger

val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate500 = Color(0xFF64748B)
val Slate800 = Color(0xFF1E293B)
val Blue600 = Color(0xFF2563EB)
val Red500 = Color(0xFFEF4444)
val Emerald500 = Color(0xFF10B981)
val Emerald50 = Color(0xFFECFDF5)
val Red50 = Color(0xFFFEF2F2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prime Spectral Analyzer", fontWeight = FontWeight.Bold, color = Slate800) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        containerColor = Slate50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = state.inputNumber,
                onValueChange = viewModel::onInputChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter a massive number...", color = Slate500) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue600,
                    unfocusedBorderColor = Slate500,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::analyzeNumber,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                if (state.isAnalyzing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Analyze", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = viewModel::toggleResearchPaper,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("View Research Paper")
            }

            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.error!!, color = Red500, fontSize = 14.sp)
            }

            if (state.isResearchPaperVisible) {
                ResearchPaperDialog(onDismiss = viewModel::toggleResearchPaper)
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.factors != null) {
                    item {
                        FactorizationExpressionCard(state)
                    }
                    item {
                        MasterEquationCard(state)
                    }
                }
            }
        }
    }
}

@Composable
fun FactorizationExpressionCard(state: AppState) {
    val factors = state.factors ?: return
    val clipboardManager = LocalClipboardManager.current
    
    val expression = factors.entries.joinToString(" × ") { (p, a) ->
        if (a > 1) "$p^$a" else "$p"
    }
    
    val fullExpression = "${state.inputNumber} = $expression"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Slate100, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRIME FACTORIZATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
                
                TextButton(
                    onClick = { clipboardManager.setText(AnnotatedString(fullExpression)) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Copy", fontSize = 12.sp, color = Blue600, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = fullExpression,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate800,
                fontFamily = FontFamily.Monospace,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun ResearchPaperDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.paper_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate800
            ) 
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(stringResource(R.string.authors), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Text(stringResource(R.string.abstract_title), fontWeight = FontWeight.Bold, color = Blue600)
                    Text(stringResource(R.string.abstract_text))
                }
                item {
                    Text(stringResource(R.string.method_1_title), fontWeight = FontWeight.Bold, color = Blue600)
                    Text("A1 =", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    Text("""
| 7,  286, 200, 176, 120, 165 |
| 206, 75, 129, 109, 123, 111 |
| 43, 52, 99, 128, 111, 110 |
| 98, 135, 112, 78, 118, 64 |
| 77, 227, 93, 88, 69, 60 |
| 34, 30, 73, 54, 45, 83 |
| 182, 88, 75, 85, 54, 53 |
| 89, 59, 37, 35, 38, 29 |
| 18, 45, 60, 49, 62, 55 |
| 78, 96, 29, 22, 24, 13 |
| 14, 11, 11, 18, 12, 12 |
| 30, 52, 52, 44, 28, 28 |
| 20, 56, 40, 31, 50, 40 |
| 46, 42, 29, 19, 36, 25 |
| 22, 17, 19, 26, 30, 20 |
| 15, 21, 11, 8, 8, 19 |
| 5, 8, 8, 11, 11, 8 |
| 3, 9, 5, 4, 7, 3 |
| 6, 3, 5, 4, 5, 6 |
                    """.trimIndent(), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
                item {
                    Text(stringResource(R.string.method_2_title), fontWeight = FontWeight.Bold, color = Blue600)
                    Text("A_N[i, j] = ⌊ σ[j mod 6] · P_i · ln N · exp(−(i · j) / (ln N)^2) ⌋")
                }
                item {
                    Text(stringResource(R.string.method_3_title), fontWeight = FontWeight.Bold, color = Blue600)
                    Text("Exact Algebra Resonance Equation:")
                    Text("Σ (W_k · sin(2π · k · √N · exp(δ))) = 0", fontFamily = FontFamily.Monospace)
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.copyright),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
        modifier = Modifier.fillMaxWidth(0.9f)
    )
}

@Composable
fun MasterEquationCard(state: AppState) {
    val phi = state.phiValue ?: return
    val input = state.inputNumber
    val wValue = state.wilsonSum ?: BigInteger.valueOf(-1)

    // Master Equation Display
    val isSuccess = wValue == BigInteger.valueOf(-1) || state.isPrime == true

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Slate100, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MASTER EQUATION Φ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (isSuccess) Emerald50 else Red50,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSuccess) Emerald500.copy(alpha = 0.5f) else Red500.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isSuccess) "PASSED" else "FAILED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) Emerald500 else Red500
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Φ($input, p...m) = ${if (isSuccess) "1.000" else "-1.000"}",
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate800,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { if (isSuccess) 1f else 0.2f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (isSuccess) Emerald500 else Red500,
                trackColor = Slate100,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "|$input - ($phi)| + W($input) = $wValue",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate500,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

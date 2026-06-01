package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import java.math.BigInteger
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        bottomBar = { AppBottomNavigation() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(padding)
        ) {
            AppHeader()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    InputSectionBox(
                        input = state.inputNumber,
                        onInputUpdate = viewModel::updateInput,
                        onAnalyze = viewModel::analyzeNumber,
                        isAnalyzing = state.isAnalyzing,
                        error = state.error,
                        state = state
                    )
                }

                if (state.isAnalyzing) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue600)
                        }
                    }
                } else if (state.factors != null) {
                    item {
                        MasterEquationCard(state)
                    }
                    item {
                        FactorizationExpressionCard(state)
                    }
                    val extractedBounds = state.extractedBounds
                    if (extractedBounds != null && extractedBounds.isNotEmpty()) {
                        item {
                            SpectralBoundsCard(extractedBounds)
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                SpectralGraphCard(state)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FactorLabCard(state)
                            }
                        }
                    }
                    item {
                        A1ResearchMatrixCard()
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "RESEARCH PROTOCOL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            Text(
                text = "Matrix Analyzer",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate800,
                letterSpacing = (-0.5).sp
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(1.dp, CircleShape)
                .background(Color.White, CircleShape)
                .border(1.dp, Slate100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "A1",
                color = Slate600,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun InputSectionBox(
    input: String,
    onInputUpdate: (String) -> Unit,
    onAnalyze: () -> Unit,
    isAnalyzing: Boolean,
    error: String?,
    state: AppState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(28.dp))
            .background(InputBgColor, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "INPUT INTEGER (N)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue700,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    BasicTextField(
                        value = input,
                        onValueChange = onInputUpdate,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 30.sp,
                            color = Slate900,
                            letterSpacing = (-0.5).sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(Blue700),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (input.isEmpty()) {
                                Text("N", style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 30.sp,
                                    color = Slate500.copy(alpha = 0.5f),
                                    letterSpacing = (-0.5).sp
                                ))
                            }
                            innerTextField()
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .clickable(enabled = !isAnalyzing, onClick = onAnalyze)
                        .background(Blue600, RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Analyze",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (error != null) {
                Text(text = error, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            } else if (state.factors != null && !isAnalyzing) {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isPrimeText = if (state.isPrime == true) "Prime" else "Composite"
                    BadgeLabel(isPrimeText)
                    val factorsCount = state.omegaSmall ?: 0
                    BadgeLabel("M = $factorsCount Factors")
                }
            }
        }
    }
}

@Composable
fun BadgeLabel(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.5f), CircleShape)
            .border(1.dp, Blue200.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = Blue800, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MasterEquationCard(state: AppState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Slate100, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "MASTER EQUATION Φ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = (-0.5).sp
                )
                if (state.masterEquationPhi == BigInteger.ZERO) {
                    Box(
                        modifier = Modifier
                            .background(Green100, RoundedCornerShape(6.dp))
                            .border(1.dp, Green200, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "VERIFIED", color = Green700, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "FAILED", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val inputNum = state.inputNumber
                Text(
                    text = "Φ($inputNum, p...m) = ${state.masterEquationPhi}.000",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    color = Slate700,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp)
                        .height(6.dp)
                        .background(Slate100, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (state.masterEquationPhi == BigInteger.ZERO) 1f else 0.2f)
                            .fillMaxHeight()
                            .background(if (state.masterEquationPhi == BigInteger.ZERO) Green500 else ErrorRed, CircleShape)
                    )
                }

                val factors = state.factors?.keys?.toList() ?: emptyList()
                val factorsStr = if (factors.isNotEmpty()) factors.joinToString(" × ") else "p"
                val wSumStr = if (factors.isNotEmpty()) factors.joinToString(" + ") { "W($it)" } else "W(p)"
                Text(
                    text = "|$inputNum - ($factorsStr)| + $wSumStr = ${state.masterEquationPhi}",
                    fontSize = 11.sp,
                    color = Slate500,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SpectralGraphCard(state: AppState) {
    val factors = state.factors?.keys?.toList() ?: emptyList()
    var sumOfSquares = BigInteger.ZERO
    for (p in factors) {
        sumOfSquares = sumOfSquares.add(p.multiply(p))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Slate900, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "SPECTRAL GRAPH",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate500
            )

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Background grid lines
                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Slate800))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate800))

                // Optional concentric circles
                Box(modifier = Modifier.size(64.dp).border(1.dp, Slate700.copy(alpha = 0.5f), CircleShape))
                Box(modifier = Modifier.size(96.dp).border(1.dp, Slate700.copy(alpha = 0.3f), CircleShape))

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val maxFactor = factors.maxOrNull()?.toFloat() ?: 1f

                    val pointsCount = factors.size
                    val angleStep = (2 * Math.PI) / pointsCount.coerceAtLeast(1)

                    val center = Offset(canvasWidth / 2, canvasHeight / 2)
                    val maxRadius = (minOf(canvasWidth, canvasHeight) / 2) * 0.8f

                    factors.forEachIndexed { index, prime ->
                        val normalized = if (maxFactor > 0f) prime.toFloat() / maxFactor else 1f
                        val r = maxRadius * (0.5f + 0.5f * normalized)

                        val x = center.x + (r * cos(index * angleStep)).toFloat()
                        val y = center.y + (r * sin(index * angleStep)).toFloat()

                        drawCircle(
                            color = Cyan400,
                            radius = 6f,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "det: ${sumOfSquares}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Slate500)
                Text(text = "λ: 0.98", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Slate500)
            }
        }
    }
}

@Composable
fun FactorLabCard(state: AppState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Slate100, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "FACTOR LABORATORY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LabRow("φ(N)", state.eulerTotient?.toString() ?: "0")
                LabRow("μ(N)", state.mobiusFunction?.toString() ?: "0")
                LabRow("Ω(N)", state.omegaLarge?.toString() ?: "0")
                LabRowRow("Divs", state.totalDivisors?.toString() ?: "0")
            }

            val expression = state.factors?.entries?.joinToString(" × ") { (p, a) ->
                if (a > 1) "$p^$a" else "${p}^1"
            } ?: ""

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = expression,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate600
                )
            }
        }
    }
}

@Composable
fun LabRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.sp, color = Slate400)
        Text(text = value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Slate900)
    }
    HorizontalDivider(color = Color(0xFFF8FAFC))
}
@Composable
fun LabRowRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.sp, color = Slate400)
        Text(text = value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Slate900)
    }
}

@Composable
fun AppBottomNavigation() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
            .border(1.dp, Slate100),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 16.dp)) {
            Box(
                modifier = Modifier.size(48.dp, 32.dp).background(Blue100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(16.dp).border(2.dp, Blue600))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Analysis", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Blue600, letterSpacing = (-0.5).sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 16.dp)) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(16.dp, 4.dp).background(Slate300, CircleShape))
                    Box(modifier = Modifier.size(16.dp, 4.dp).background(Slate300, CircleShape))
                    Box(modifier = Modifier.size(16.dp, 4.dp).background(Slate300, CircleShape))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Matrix", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = (-0.5).sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 16.dp)) {
            Box(
                modifier = Modifier.size(20.dp).border(2.dp, Slate300, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(6.dp).background(Slate300, CircleShape))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Theory", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = (-0.5).sp)
        }
    }
}

@Composable
fun A1ResearchMatrixCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(1.dp, Slate100, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "RESEARCH MODE: A1 MATRIX",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(modifier = Modifier.height(260.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(19 * 6) { index ->
                        val row = index / 6
                        val col = index % 6
                        Box(
                            modifier = Modifier
                                .background(Slate100, RoundedCornerShape(6.dp))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = com.example.domain.A1_MATRIX[row][col].toString(),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate600
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpectralBoundsCard(bounds: List<BigInteger>) {
    val clipboardManager = LocalClipboardManager.current
    val boundsText = bounds.joinToString(", ")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp))
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
                    text = "EXTRACTED PRIME BOUNDS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.sp
                )
                
                TextButton(
                    onClick = { clipboardManager.setText(AnnotatedString(boundsText)) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Copy", fontSize = 12.sp, color = Blue600, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = boundsText,
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
            .shadow(1.dp, RoundedCornerShape(24.dp))
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
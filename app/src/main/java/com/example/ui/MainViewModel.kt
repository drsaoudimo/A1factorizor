package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.NumberTheoryEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger

data class AppState(
    val inputNumber: String = "",
    val isAnalyzing: Boolean = false,
    val factors: Map<BigInteger, Int>? = null,
    val isPrime: Boolean? = null,
    val wilsonSum: BigInteger? = null,
    val phiValue: BigInteger? = null,
    val error: String? = null,
    val isResearchPaperVisible: Boolean = false
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppState())
    val uiState = _uiState.asStateFlow()

    fun onInputChanged(newInput: String) {
        val sanitized = newInput.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(inputNumber = sanitized, error = null)
    }

    fun toggleResearchPaper() {
        _uiState.value = _uiState.value.copy(isResearchPaperVisible = !_uiState.value.isResearchPaperVisible)
    }

    fun analyzeNumber() {
        val currentInput = _uiState.value.inputNumber
        if (currentInput.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            isAnalyzing = true,
            factors = emptyMap(),
            isPrime = null,
            wilsonSum = null,
            phiValue = null,
            error = null
        )

        viewModelScope.launch {
            try {
                val number = BigInteger(currentInput)
                if (number <= BigInteger.ZERO) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = "Please enter a positive integer > 0"
                    )
                    return@launch
                }

                val currentFactors = mutableMapOf<BigInteger, Int>()
                
                withContext(Dispatchers.Default) {
                    val isPrimeCheck = NumberTheoryEngine.isPrime(number)
                    
                    NumberTheoryEngine.spectralAnalyzer(number) { factor ->
                        currentFactors[factor] = (currentFactors[factor] ?: 0) + 1
                        _uiState.value = _uiState.value.copy(factors = currentFactors.toSortedMap())
                    }
                    
                    val phi = NumberTheoryEngine.calculatePhi(number, currentFactors)
                    val wilsonSumRes = NumberTheoryEngine.wilsonSum(number)

                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        isPrime = isPrimeCheck,
                        wilsonSum = wilsonSumRes,
                        phiValue = phi
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = e.localizedMessage
                )
            }
        }
    }
}

package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.NumberTheoryEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger

data class AppState(
    val inputNumber: String = "",
    val isAnalyzing: Boolean = false,
    val extractedBounds: List<BigInteger>? = null,
    val factors: Map<BigInteger, Int>? = null,
    val isPrime: Boolean? = null,
    val wilsonSum: BigInteger? = null,
    val masterEquationPhi: BigInteger? = null,
    val eulerTotient: BigInteger? = null,
    val mobiusFunction: Int? = null,
    val omegaSmall: Int? = null,
    val omegaLarge: Int? = null,
    val totalDivisors: BigInteger? = null,
    val error: String? = null
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun updateInput(input: String) {
        _state.update { it.copy(inputNumber = input, error = null) }
    }

    fun analyzeNumber() {
        val currentInput = _state.value.inputNumber.trim()
        if (currentInput.isEmpty()) {
            _state.update { it.copy(error = "Input cannot be empty") }
            return
        }

        val number = try {
            BigInteger(currentInput)
        } catch (e: NumberFormatException) {
            _state.update { it.copy(error = "Invalid number format") }
            return
        }

        if (number <= BigInteger.ZERO) {
            _state.update { it.copy(error = "Please enter a positive integer > 0") }
            return
        }

        _state.update { it.copy(isAnalyzing = true, error = null) }

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    val isPrimeCheck = NumberTheoryEngine.isPrime(number)
                    val factorResult = NumberTheoryEngine.factorize(number)
                    val boundsResult = NumberTheoryEngine.extractPrimeBounds(number).sorted()
                    val phi = NumberTheoryEngine.calculatePhi(number, factorResult)
                    
                    var wilsonSumRes = BigInteger.ZERO
                    for (p in factorResult.keys) {
                        wilsonSumRes = wilsonSumRes.add(NumberTheoryEngine.wilson(p))
                    }
                    
                    val euler = NumberTheoryEngine.eulerTotient(number, factorResult)
                    val mobius = NumberTheoryEngine.mobius(factorResult)
                    val oSmall = NumberTheoryEngine.omegaSmall(factorResult)
                    val oLarge = NumberTheoryEngine.omegaLarge(factorResult)
                    val divisors = NumberTheoryEngine.divisorsCount(factorResult)

                    AppState(
                        inputNumber = currentInput,
                        isAnalyzing = false,
                        extractedBounds = boundsResult,
                        factors = factorResult,
                        isPrime = isPrimeCheck,
                        wilsonSum = wilsonSumRes,
                        masterEquationPhi = phi,
                        eulerTotient = euler,
                        mobiusFunction = mobius,
                        omegaSmall = oSmall,
                        omegaLarge = oLarge,
                        totalDivisors = divisors
                    )
                }
                _state.value = result
            } catch (e: Exception) {
                _state.update { it.copy(isAnalyzing = false, error = "Analysis failed: ${e.message}") }
            }
        }
    }
}

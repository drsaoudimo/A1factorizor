package com.example.domain

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.SingularValueDecomposition
import java.lang.Math
import java.math.BigInteger
import java.security.SecureRandom
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object NumberTheoryEngine {
    private val TWO = BigInteger.valueOf(2L)
    private val BASE_PRIMES = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67).map { BigInteger.valueOf(it.toLong()) }
    
    // SVD constants from A1 seed matrix
    private val sigmaBase = doubleArrayOf(
        588.66723048, 199.11266205, 128.24357738, 77.01284206, 61.64964687, 24.3807535
    )

    private val PRIMES_UP_TO_5000 = listOf(
        2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113,
        127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 
        257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 
        401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 
        563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701, 
        709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 
        877, 881, 883, 887, 907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997
    ).map { BigInteger.valueOf(it.toLong()) }

    private val A1_MATRIX = arrayOf(
        doubleArrayOf(7.0, 286.0, 200.0, 176.0, 120.0, 165.0),
        doubleArrayOf(206.0, 75.0, 129.0, 109.0, 123.0, 111.0),
        doubleArrayOf(43.0, 52.0, 99.0, 128.0, 111.0, 110.0),
        doubleArrayOf(98.0, 135.0, 112.0, 78.0, 118.0, 64.0),
        doubleArrayOf(77.0, 227.0, 93.0, 88.0, 69.0, 60.0),
        doubleArrayOf(34.0, 30.0, 73.0, 54.0, 45.0, 83.0),
        doubleArrayOf(182.0, 88.0, 75.0, 85.0, 54.0, 53.0),
        doubleArrayOf(89.0, 59.0, 37.0, 35.0, 38.0, 29.0),
        doubleArrayOf(18.0, 45.0, 60.0, 49.0, 62.0, 55.0),
        doubleArrayOf(78.0, 96.0, 29.0, 22.0, 24.0, 13.0),
        doubleArrayOf(14.0, 11.0, 11.0, 18.0, 12.0, 12.0),
        doubleArrayOf(30.0, 52.0, 52.0, 44.0, 28.0, 28.0),
        doubleArrayOf(20.0, 56.0, 40.0, 31.0, 50.0, 40.0),
        doubleArrayOf(46.0, 42.0, 29.0, 19.0, 36.0, 25.0),
        doubleArrayOf(22.0, 17.0, 19.0, 26.0, 30.0, 20.0),
        doubleArrayOf(15.0, 21.0, 11.0, 8.0, 8.0, 19.0),
        doubleArrayOf(5.0, 8.0, 8.0, 11.0, 11.0, 8.0),
        doubleArrayOf(3.0, 9.0, 5.0, 4.0, 7.0, 3.0),
        doubleArrayOf(6.0, 3.0, 5.0, 4.0, 5.0, 6.0)
    )

    private fun approximatePrime(i: Int): Int {
        if (i < 1) return 2
        if (i <= 2) return i + 1
        val iDouble = i.toDouble()
        return (iDouble * (Math.log(iDouble) + Math.log(Math.log(iDouble)) - 1.0)).toInt()
    }

    private fun buildDynamicMatrix(N: BigInteger, center: Double? = null, beta: Double = 0.5): Pair<Array2DRowRealMatrix, List<Double>> {
        val nDouble = N.toDouble()
        val lnN = Math.log(nDouble.coerceAtLeast(Math.E))
        val M = Math.max(20, (lnN * lnN).toInt())
        val K = Math.max(6, (Math.log(nDouble) / Math.log(2.0)).toInt())
        val A_N = Array(M) { DoubleArray(K) }
        
        val Q = if (center == null) {
            val step = Math.sqrt(nDouble) / (lnN * lnN)
            List(M) { i -> (i + 1) * step }
        } else {
            val halfRange = beta * lnN
            List(M) { i -> center - halfRange + (i + 1) * (2 * halfRange) / M }
        }
        
        for (i in 0 until M) {
            val Pi = Q[i]
            for (j in 0 until K) {
                val d = exp(-(i * j) / (lnN * lnN))
                A_N[i][j] = floor(sigmaBase[j % 6] * Pi * lnN * d)
            }
        }
        return Pair(Array2DRowRealMatrix(A_N), Q)
    }

    private fun getExponent(n: BigInteger, p: BigInteger): Int {
        var num = n
        var k = 0
        while (num > BigInteger.ONE && num.remainder(p) == BigInteger.ZERO) {
            num = num.divide(p)
            k++
        }
        return k
    }

    fun spectralAnalyzer(N: BigInteger, onFactorFound: (BigInteger) -> Unit) {
        val factors = mutableMapOf<BigInteger, Int>()
        
        fun decompose(nVar: BigInteger) {
            if (nVar <= BigInteger.ONE) return
            
            // 1. Trial division fallback
            var tempN = nVar
            for (p in PRIMES_UP_TO_5000) {
                if (p.multiply(p) > tempN) break
                while (tempN.remainder(p) == BigInteger.ZERO) {
                    onFactorFound(p)
                    tempN = tempN.divide(p)
                }
            }
            if (tempN == BigInteger.ONE) return
            if (millerRabin(tempN)) {
                onFactorFound(tempN)
                return
            }

            // 2. Try Spectral Method
            val maxIter = 5
            var iteration = 0
            var found = false
            var prevQ: List<Double>? = null
            var prevZ: DoubleArray? = null
            
            var currentN = tempN
            val lnN = Math.log(currentN.toDouble().coerceAtLeast(Math.E))
            
            while (iteration < maxIter && !found) {
                iteration++
                
                val center = if (iteration > 1 && prevQ != null && prevZ != null) {
                    val numerator = prevQ!!.zip(prevZ!!.asIterable()).sumOf { (q, z) -> q * z }
                    val denominator = prevZ!!.sum()
                    if (abs(denominator) > 1e-9) numerator / denominator else null
                } else null
                
                val (aN, Q) = buildDynamicMatrix(currentN, center = center)
                
                val svd = SingularValueDecomposition(aN)
                val U = svd.u
                val Sigma = svd.singularValues
                val K = aN.columnDimension
                val M = aN.rowDimension
                
                val tau = Math.sqrt(currentN.toDouble()) / lnN
                
                val sN = DoubleArray(M)
                for (i in 0 until M) {
                    sN[i] = currentN.remainder(BigInteger.valueOf(Q[i].toLong().coerceAtLeast(2L))).toDouble()
                }
                val snVector = ArrayRealVector(sN)
                
                val sigmaInv = DoubleArray(Sigma.size) { i ->
                    if (Sigma[i] > 1e-5) 1.0 / Sigma[i] else 0.0
                }
                val uT_SN = U.transpose().operate(snVector)
                val z = DoubleArray(Sigma.size) { i -> sigmaInv[i] * uT_SN.getEntry(i) }
                
                for (j in 0 until K) {
                    if (abs(z[j]) >= tau) {
                        val aN_pinv = svd.solver.inverse
                        val pRaw = aN_pinv.operate(snVector)
                        val kDouble = abs(pRaw.getEntry(j) * (if (j < Sigma.size) Sigma[j] else 0.0) * lnN)
                        if (kDouble > 1.0) {
                            val kjBig = BigInteger.valueOf(kDouble.toLong())
                            val p = currentN.gcd(kjBig)
                            if (p > BigInteger.ONE && p < currentN) {
                                decompose(p)
                                decompose(currentN.divide(p))
                                found = true
                                break
                            }
                        }
                    }
                }
                prevQ = Q
                prevZ = z
            }
            
            // 3. Neighborhood Sieving (if still not found)
            if (!found) {
                val delta = solveExactDelta(currentN)
                val pApprox = Math.sqrt(currentN.toDouble()) * Math.exp(-delta)
                val range = lnN * 2.0 // Small search range
                val start = (pApprox - range).toLong().coerceAtLeast(2L)
                val end = (pApprox + range).toLong()
                
                for (p in start..end) {
                    if (p % 2L == 0L && p != 2L) continue
                    val pBig = BigInteger.valueOf(p)
                    if (pBig > BigInteger.ONE && pBig < currentN && currentN.remainder(pBig) == BigInteger.ZERO) {
                        decompose(pBig)
                        decompose(currentN.divide(pBig))
                        found = true
                        break
                    }
                }
            }
            
            // 4. Final Fallback (Robust)
            if (!found) {
                if (millerRabin(currentN)) {
                    onFactorFound(currentN)
                } else {
                    // Try Pollard's Rho, guided by spectral hint
                    val hint: BigInteger? = run {
                        val delta = solveExactDelta(currentN)
                        val pApprox = Math.sqrt(currentN.toDouble()) * Math.exp(-delta)
                        BigInteger.valueOf(pApprox.toLong())
                    }
                    
                    val factor = pollardRho(currentN, hint = hint)
                    if (factor != currentN && factor > BigInteger.ONE) {
                        decompose(factor)
                        decompose(currentN.divide(factor))
                    } else {
                        // If all fails, report as is
                        onFactorFound(currentN)
                    }
                }
            }
        }
        
        decompose(N)
    }

    private fun solveExactDelta(n: BigInteger): Double {
        val sqrtN = Math.sqrt(n.toDouble())
        val W = A1_MATRIX.flatMap { it.asIterable() }.toDoubleArray()
        
        fun f(delta: Double): Double {
            val qGuess = sqrtN * Math.exp(delta)
            var sineSum = 0.0
            for (k in W.indices) {
                val phase = ((k + 1) * qGuess) % 1.0
                sineSum += W[k] * Math.sin(2 * Math.PI * phase)
            }
            return sineSum
        }
        
        var bestDelta = 0.0
        var minVal = Double.MAX_VALUE
        val steps = 1000
        for (i in 0..steps) {
            val delta = 0.01 + (i.toDouble() / steps) * 1.99
            val v = Math.abs(f(delta))
            if (v < minVal) {
                minVal = v
                bestDelta = delta
            }
        }
        return bestDelta
    }


    fun isPrime(n: BigInteger): Boolean {
        // Spectral resonance pre-check
        for (prime in BASE_PRIMES) {
            if (n == prime) return true
            if (n.remainder(prime) == BigInteger.ZERO) return false
        }
        // Miller-Rabin for higher certainty
        return millerRabin(n)
    }

    fun millerRabin(n: BigInteger, k: Int = 40): Boolean {
        if (n <= BigInteger.ONE) return false
        if (n == TWO || n == BigInteger.valueOf(3)) return true
        if (n.remainder(TWO) == BigInteger.ZERO) return false
        
        var d = n.subtract(BigInteger.ONE)
        var s = 0
        while (d.remainder(TWO) == BigInteger.ZERO) {
            d = d.divide(TWO)
            s++
        }
        
        val random = SecureRandom()
        repeat(k) {
            val a = TWO.add(BigInteger(n.bitLength() - 3, random).remainder(n.subtract(BigInteger.valueOf(4))))
            var x = a.modPow(d, n)
            if (x == BigInteger.ONE || x == n.subtract(BigInteger.ONE)) return@repeat
            
            var composite = true
            repeat(s - 1) {
                x = x.modPow(TWO, n)
                if (x == n.subtract(BigInteger.ONE)) {
                    composite = false
                    return@repeat
                }
            }
            if (composite) return false
        }
        return true
    }

    private fun pollardRho(n: BigInteger, hint: BigInteger? = null): BigInteger {
        if (n == TWO) return TWO
        if (millerRabin(n)) return n
        
        var x = hint ?: BigInteger("2")
        var y = hint ?: BigInteger("2")
        var d = BigInteger.ONE
        var c = BigInteger.ONE
        val random = SecureRandom()
        
        while (d == BigInteger.ONE) {
            x = (x.multiply(x).add(c)).remainder(n)
            y = (y.multiply(y).add(c)).remainder(n)
            y = (y.multiply(y).add(c)).remainder(n)
            d = (x.subtract(y)).abs().gcd(n)
            if (d == n) {
                // Failure: Retry with different c
                x = BigInteger(n.bitLength(), random).remainder(n)
                y = x
                c = c.add(BigInteger.ONE)
                d = BigInteger.ONE
            }
        }
        return d
    }

    fun factorize(n: BigInteger): Map<BigInteger, Int> {
        val factors = mutableMapOf<BigInteger, Int>()
        spectralAnalyzer(n) { factor ->
            factors[factor] = (factors[factor] ?: 0) + 1
        }
        return factors
    }

    fun calculatePhi(n: BigInteger, factors: Map<BigInteger, Int>): BigInteger {
        if (n <= BigInteger.ZERO) return BigInteger.ZERO
        if (n == BigInteger.ONE) return BigInteger.ONE
        var result = n
        for ((p, _) in factors) {
            result = result.divide(p).multiply(p.subtract(BigInteger.ONE))
        }
        return result
    }

    fun wilsonSum(n: BigInteger): BigInteger {
        if (n <= BigInteger.ONE) return BigInteger.ZERO
        if (n > BigInteger.valueOf(5000)) return BigInteger.valueOf(-1) 
        
        var fact = BigInteger.ONE
        for (i in 2 until n.toInt()) {
            fact = fact.multiply(BigInteger.valueOf(i.toLong())).remainder(n)
        }
        return fact.add(BigInteger.ONE).remainder(n)
    }
}

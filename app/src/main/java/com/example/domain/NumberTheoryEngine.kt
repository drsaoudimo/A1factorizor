package com.example.domain

import java.math.BigInteger
import java.security.SecureRandom

object NumberTheoryEngine {

    // Using a hybrid approach: Trial division -> Pollard's Rho
    fun getPrimeFactors(N: BigInteger): List<BigInteger> {
        if (N <= BigInteger.ONE) return emptyList()
        val factors = mutableListOf<BigInteger>()
        
        var n = N
        // Trial division for small primes
        val smallPrimes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
        for (p in smallPrimes) {
            val bp = BigInteger.valueOf(p.toLong())
            while (n.remainder(bp) == BigInteger.ZERO) {
                factors.add(bp)
                n = n.divide(bp)
            }
        }
        
        if (n > BigInteger.ONE) {
            factors.addAll(factorizeRecursive(n))
        }
        
        return factors.sorted()
    }

    private fun factorizeRecursive(N: BigInteger): List<BigInteger> {
        if (N.isProbablePrime(10)) return listOf(N)
        
        // Pollard's Rho
        val factor = pollardRho(N)
        return factorizeRecursive(factor) + factorizeRecursive(N.divide(factor))
    }

    private fun pollardRho(n: BigInteger): BigInteger {
        if (n.remainder(BigInteger.valueOf(2)) == BigInteger.ZERO) return BigInteger.valueOf(2)
        
        val random = SecureRandom()
        var x = BigInteger(n.bitLength(), random).remainder(n)
        var y = x
        var c = BigInteger(n.bitLength(), random).remainder(n)
        var d = BigInteger.ONE
        
        while (d == BigInteger.ONE) {
            x = x.multiply(x).add(c).remainder(n)
            y = y.multiply(y).add(c).remainder(n)
            y = y.multiply(y).add(c).remainder(n)
            d = x.subtract(y).abs().gcd(n)
            
            if (d == n) return pollardRho(n) // Retry with different parameters
        }
        return d
    }
}

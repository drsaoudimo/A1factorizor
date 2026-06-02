package com.example.domain

import java.math.BigInteger
import java.util.*

object NumberTheoryEngine {

    fun getPrimeFactors(N: BigInteger): List<BigInteger> {
        val factors = mutableListOf<BigInteger>()
        var n = N
        
        if (n <= BigInteger.ONE) return emptyList()
        
        // Trial division for small factors
        var i = BigInteger.valueOf(2)
        while (i.multiply(i) <= n && i <= BigInteger.valueOf(1000)) {
            while (n.remainder(i) == BigInteger.ZERO) {
                factors.add(i)
                n = n.divide(i)
            }
            i = i.add(BigInteger.ONE)
        }
        
        if (n > BigInteger.ONE) {
            if (n.isProbablePrime(10)) {
                factors.add(n)
            } else {
                factors.addAll(factorizeRecursive(n))
            }
        }
        
        return factors
    }

    private fun factorizeRecursive(N: BigInteger): List<BigInteger> {
        if (N.isProbablePrime(10)) return listOf(N)
        val factor = pollardRho(N)
        return factorizeRecursive(factor) + factorizeRecursive(N.divide(factor))
    }

    private fun pollardRho(n: BigInteger): BigInteger {
        if (n.remainder(BigInteger.valueOf(2)) == BigInteger.ZERO) return BigInteger.valueOf(2)
        var x = BigInteger("2")
        var y = BigInteger("2")
        var d = BigInteger.ONE
        var c = BigInteger.ONE
        val f = { x: BigInteger -> x.multiply(x).add(c).remainder(n) }

        while (d == BigInteger.ONE) {
            x = f(x)
            y = f(f(y))
            d = x.subtract(y).abs().gcd(n)
            if (d == n) { // Failure, try another c
                c = c.add(BigInteger.ONE)
                x = BigInteger("2")
                y = BigInteger("2")
                d = BigInteger.ONE
            }
        }
        return d
    }
}

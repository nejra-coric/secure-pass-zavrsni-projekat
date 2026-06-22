package com.nejracoric.securepassandroid.security

enum class PwnedNetworkIssue {
    Ok,
    NoInternet,
    HibpBlocked,
}

object PwnedNetworkDiagnostics {

    fun diagnose(): PwnedNetworkIssue {
        val internetWorks = PwnedPasswordChecker.canReach("https://www.google.com/generate_204")
        if (!internetWorks) return PwnedNetworkIssue.NoInternet

        val hibpWorks = PwnedPasswordChecker.canReach("https://api.pwnedpasswords.com/range/5BAA6")
        return if (hibpWorks) PwnedNetworkIssue.Ok else PwnedNetworkIssue.HibpBlocked
    }
}

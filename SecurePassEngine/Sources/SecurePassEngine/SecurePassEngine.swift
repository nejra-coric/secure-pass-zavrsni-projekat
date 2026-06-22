@_silgen_name("log")
private func libcLog(_ value: Double) -> Double

public enum SecurePassError: Int32 {
    case success = 0
    case invalidParameter = -1
    case bufferTooSmall = -2
}

public enum SecurePassEngine {

    private static let lowercase = Array("abcdefghijklmnopqrstuvwxyz")
    private static let uppercase = Array("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
    private static let digits = Array("0123456789")
    private static let special = Array("!@#$%^&*()-_=+[]{}|;:,.<>?")

    public static func generatePassword(length: Int, includeSpecialChars: Bool) -> String? {
        guard length > 0 else { return nil }

        var pools: [[Character]] = [lowercase, uppercase, digits]
        if includeSpecialChars {
            pools.append(special)
        }

        let charset = pools.flatMap { $0 }
        guard !charset.isEmpty else { return nil }

        var rng = SystemRandomNumberGenerator()
        var password = [Character](repeating: charset[0], count: length)

        let guaranteedCount = min(length, pools.count)
        for index in 0..<guaranteedCount {
            password[index] = pools[index].randomElement(using: &rng)!
        }

        for index in guaranteedCount..<length {
            password[index] = charset.randomElement(using: &rng)!
        }

        for index in stride(from: length - 1, through: 1, by: -1) {
            let swapIndex = Int.random(in: 0...index, using: &rng)
            password.swapAt(index, swapIndex)
        }

        return String(password)
    }

    public static func calculateEntropy(password: String) -> Double {
        guard !password.isEmpty else { return 0 }

        var poolSize = 0
        var hasLower = false
        var hasUpper = false
        var hasDigit = false
        var hasSpecial = false

        for scalar in password.unicodeScalars {
            switch scalar.value {
            case 97...122: hasLower = true
            case 65...90: hasUpper = true
            case 48...57: hasDigit = true
            default: hasSpecial = true
            }
        }

        if hasLower { poolSize += lowercase.count }
        if hasUpper { poolSize += uppercase.count }
        if hasDigit { poolSize += digits.count }
        if hasSpecial { poolSize += special.count }

        guard poolSize > 0 else { return 0 }

        return Double(password.count) * entropyLog2(Double(poolSize))
    }

    public static func charsetSize(includeSpecialChars: Bool) -> Int {
        var size = lowercase.count + uppercase.count + digits.count
        if includeSpecialChars {
            size += special.count
        }
        return size
    }

    private static func entropyLog2(_ value: Double) -> Double {
        guard value > 0 else { return 0 }
        return libcLog(value) / libcLog(2.0)
    }
}

@_cdecl("securepass_generate_password")
public func securepass_generate_password(
    _ length: Int32,
    _ includeSpecial: Int32,
    _ buffer: UnsafeMutablePointer<CChar>?,
    _ bufferSize: Int32
) -> Int32 {
    guard let buffer, bufferSize > 0, length > 0 else {
        return SecurePassError.invalidParameter.rawValue
    }

    let includeSpecialChars = includeSpecial != 0
    guard let password = SecurePassEngine.generatePassword(
        length: Int(length),
        includeSpecialChars: includeSpecialChars
    ) else {
        return SecurePassError.invalidParameter.rawValue
    }

    let utf8 = Array(password.utf8CString)
    guard utf8.count <= bufferSize else {
        return SecurePassError.bufferTooSmall.rawValue
    }

    for index in 0..<utf8.count {
        buffer[index] = utf8[index]
    }

    return SecurePassError.success.rawValue
}

@_cdecl("securepass_calculate_entropy")
public func securepass_calculate_entropy(_ password: UnsafePointer<CChar>?) -> Double {
    guard let password else { return 0 }
    return SecurePassEngine.calculateEntropy(password: String(cString: password))
}

@_cdecl("securepass_charset_size")
public func securepass_charset_size(_ includeSpecial: Int32) -> Int32 {
    Int32(SecurePassEngine.charsetSize(includeSpecialChars: includeSpecial != 0))
}

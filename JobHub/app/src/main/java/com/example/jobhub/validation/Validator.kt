package com.example.jobhub.validation

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

fun validateEmail(email: String): ValidationResult {
    return if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        ValidationResult.Success
    } else {
        ValidationResult.Error("Valid email")
    }
}

fun validatePassword(password: String): ValidationResult {
    if (password.length < 8 || password.length > 16) {
        return ValidationResult.Error("Password must be between 8 and 16 characters long")
    }

    if (!password.any { it.isUpperCase() }) {
        return ValidationResult.Error("Password must contain at least one uppercase letter")
    }

    if (!password.any { it.isLowerCase() }) {
        return ValidationResult.Error("Password must contain at least one lowercase letter")
    }

    if (!password.any { it.isDigit() }) {
        return ValidationResult.Error("Password must contain at least one digit")
    }

    val specialChars = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/`~"
    if (!password.any { it in specialChars }) {
        return ValidationResult.Error("Password must contain at least one special character")
    }
    return ValidationResult.Success
}

fun validateTitle(title: String): ValidationResult {
    if (title.length < 8 || title.length > 27) {
        return ValidationResult.Error("Title must be between 8 and 27 characters long")
    }

    if (title.any { it.isDigit() }) {
        return ValidationResult.Error("Title must not contain digit")
    }

    val specialChars = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/`~"
    if (title.any { it in specialChars }) {
        return ValidationResult.Error("Title must not contain special character")
    }
    return ValidationResult.Success
}

fun validateSalary(salary: String): ValidationResult {
    val salaryValue = salary.toDoubleOrNull()
    if (salaryValue != null) {
        if (salaryValue.equals(0.0)) {
            return ValidationResult.Error("Salary must be above 0$")
        }
    }
    return ValidationResult.Success
}

fun validateCompanyName(companyName: String): ValidationResult {
    if (companyName.length < 8 || companyName.length > 30) {
        return ValidationResult.Error("Company name must be between 8 and 30 characters long")
    }

    if (companyName.any { it.isDigit() }) {
        return ValidationResult.Error("Company name must not contain digit")
    }

    val specialChars = "!@#$%^&*()-_=+[]{}|;:'\",.<>?/`~"
    if (companyName.any { it in specialChars }) {
        return ValidationResult.Error("Company name must not contain special character")
    }
    return ValidationResult.Success
}

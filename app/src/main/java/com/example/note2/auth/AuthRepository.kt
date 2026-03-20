package com.example.note2.auth

class AuthRepository {
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email == "abc@gmail.com" && password == "12345678") {
            onResult(true, null)
        } else {
            onResult(false, "Email hoặc mật khẩu không đúng")
        }

    }
    fun register(
        email: String,
        password: String,
        onResult: (Boolean,String?) -> Unit
    ){
        if(email.isNotBlank() && password.length >= 6){
            onResult(true,null)
        }
        else {
            onResult(false,"Email hoặc mật khẩu không hợp lệ")
        }
    
    }
}
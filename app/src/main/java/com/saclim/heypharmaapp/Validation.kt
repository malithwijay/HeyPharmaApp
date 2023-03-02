package com.saclim.heypharmaapp

class Validation {
    fun isNumeric(valueToCheck:String):Boolean{
        return valueToCheck.any{char -> char.isDigit()}
    }
}
package com.saclim.heypharmaapp

class Member {
    lateinit var name:String
    lateinit var dob:String
    var telephone:Int=0
    lateinit var address:String
    lateinit var email:String

    constructor(name: String, dob: String, telephone: Int, address: String, email: String) {
        this.name = name
        this.dob = dob
        this.telephone = telephone
        this.address = address
        this.email = email
    }

    constructor()
}
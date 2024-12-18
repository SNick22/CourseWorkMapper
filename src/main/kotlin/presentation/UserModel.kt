package org.example.presentation

import Mapper
import org.example.domain.User

@Mapper(fromClasses = [User::class])
data class UserModel(
    val name: String,
    val surname: String
)

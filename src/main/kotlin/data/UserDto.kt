package org.example.data

import Mapper
import NewName
import org.example.domain.User
import org.example.domain.User2

@Mapper(toClasses = [User::class, User2::class])
data class UserDto(
    val name: String,
    val surname: String,
    @NewName(["busy"])
    val isBusy: Boolean = true
)

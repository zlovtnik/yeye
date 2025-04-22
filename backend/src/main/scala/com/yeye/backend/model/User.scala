package com.yeye.backend.model

import com.yeye.shared.User
import caliban.schema.{Schema, SchemaDerivation}
import caliban.schema.ArgBuilder

object UserModel:
  given [R]: Schema[R, User] = Schema.gen[R, User]
  given ArgBuilder[User] = ArgBuilder.gen[User]

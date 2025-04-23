package com.yeye.backend.model

import com.yeye.shared.User
import caliban.schema.{Schema, SchemaDerivation}
import caliban.schema.ArgBuilder

/** Model object providing schema instances for the User type.
  *
  * This object defines:
  *   - GraphQL schema for User type
  *   - Argument builder for User type
  */
object UserModel:
  /** Schema instance for User type that can be used with any environment R */
  given [R]: Schema[R, User] = Schema.gen[R, User]

  /** Argument builder for User type to handle GraphQL input */
  given ArgBuilder[User] = ArgBuilder.gen[User]

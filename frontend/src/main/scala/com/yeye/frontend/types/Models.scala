package com.yeye.frontend.types

import zio.json.*

/** Represents a file in the system */
case class File(
    id: String,
    name: String,
    size: Long,
    fileType: String,
    lastModified: Long
)

object File:
  implicit val codec: JsonCodec[File] = DeriveJsonCodec.gen[File]

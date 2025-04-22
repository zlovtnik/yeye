package com.yeye.shared

import zio.json.*

case class User(
    id: String,
    email: String,
    firstName: String,
    lastName: String,
    status: String,
    created: Long,
    lastUpdated: Long
) derives JsonCodec

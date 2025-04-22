package com.yeye.shared

import zio.json.*

case class Process(
    id: String,
    name: String,
    status: ProcessStatus,
    createdAt: Long,
    updatedAt: Long
) derives JsonCodec

enum ProcessStatus derives JsonCodec:
  case Running, Paused, Completed, Failed

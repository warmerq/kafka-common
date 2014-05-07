package com.github.warmerq.common.util

import java.security.MessageDigest

object MD5 {
  def apply(s: String) = {
    MessageDigest.getInstance("MD5").digest(s.getBytes)
  }
}

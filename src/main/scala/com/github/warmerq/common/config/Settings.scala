package com.github.warmerq.common.config

import com.typesafe.config.ConfigFactory

trait Settings {

  val conf = ConfigFactory.load

}

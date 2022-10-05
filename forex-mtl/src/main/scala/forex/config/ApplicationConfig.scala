package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneframe: OneFrameConfig,
    rateTtl: FiniteDuration,
    cacheUpdateInterval: FiniteDuration,
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class OneFrameConfig(
    address: String,
    token: String,
)

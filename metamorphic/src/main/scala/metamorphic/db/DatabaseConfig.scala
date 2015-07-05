package metamorphic.db

case class DatabaseConfig(
  system: DatabaseSystem,
  name: String,
  user: String,
  password: String,
  host: String,
  port: Int,
  numThreads: Int,
  queueSize: Int)

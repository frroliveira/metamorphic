package metamorphic

package object db {

  type DatabaseSystem = String

  val AccessSystem: DatabaseSystem = "Access"
  val DB2System: DatabaseSystem = "DB2"
  val H2System: DatabaseSystem = "H2"
  val HyperSQLSystem: DatabaseSystem = "HyperSQL"
  val MySQLSystem: DatabaseSystem = "MySQL"
  val OracleSystem: DatabaseSystem = "Oracle"
  val PostgreSQLSystem: DatabaseSystem = "PostgreSQL"
  val SQLiteSystem: DatabaseSystem = "SQLite"
  val SQLServerSystem: DatabaseSystem = "SQLServer"

  object DatabaseSystem {

    def all: List[DatabaseSystem] = {
      List(AccessSystem, DB2System, H2System, HyperSQLSystem, MySQLSystem,
        OracleSystem, PostgreSQLSystem, SQLiteSystem, SQLServerSystem)
    }
  }
}

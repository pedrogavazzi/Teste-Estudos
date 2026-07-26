package com.pedrogavazzi.controleestudos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Materia::class, Aula::class, FotoAula::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun materiaDao(): MateriaDao
    abstract fun aulaDao(): AulaDao
    abstract fun fotoAulaDao(): FotoAulaDao

    companion object {

        /** Adiciona a coluna de tipo de alerta (som/vibração/ambos) por aula, preservando os dados existentes. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE aulas ADD COLUMN tipoAlerta TEXT NOT NULL DEFAULT 'SOM_E_VIBRACAO'"
                )
            }
        }

        /** Adiciona a coluna de anotações longas do caderno, separada da observação curta. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE aulas ADD COLUMN anotacoesCaderno TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        /** Adiciona a coluna de nome customizado por aula (opcional). */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE aulas ADD COLUMN nomePersonalizado TEXT")
            }
        }

        /** Adiciona a coluna de link por aula (opcional — videochamada, gravação, material). */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE aulas ADD COLUMN link TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Cria a tabela de fotos/prints anexados a uma aula (guarda só o nome do arquivo,
         *  a imagem em si fica no armazenamento local do app, nunca no banco). */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `fotos_aula` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `aulaId` INTEGER NOT NULL,
                        `nomeArquivo` TEXT NOT NULL,
                        `criadaEmMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fotos_aula_aulaId` ON `fotos_aula` (`aulaId`)")
            }
        }

        /** Adiciona a marcação de "favorita" por aula, usada pela aba Favoritos. */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE aulas ADD COLUMN favorita INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "controle_estudos.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build().also { INSTANCE = it }
            }
        }
    }
}

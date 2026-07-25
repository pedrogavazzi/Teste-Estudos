package com.pedrogavazzi.controleestudos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Materia::class, Aula::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun materiaDao(): MateriaDao
    abstract fun aulaDao(): AulaDao

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

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "controle_estudos.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build().also { INSTANCE = it }
            }
        }
    }
}

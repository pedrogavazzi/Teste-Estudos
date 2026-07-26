package com.pedrogavazzi.controleestudos.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Uma foto/print anexado a uma aula — guardada só localmente, no armazenamento privado do
 * app (pasta "fotos_aulas" dentro do armazenamento interno). Essa tabela guarda só a
 * referência (nome do arquivo); o conteúdo da imagem em si nunca fica no banco.
 *
 * Sem chave estrangeira de propósito (o resto do banco também não usa) — a limpeza dos
 * arquivos e das linhas quando uma matéria é excluída é feita manualmente no repositório,
 * do mesmo jeito que já é feito para as aulas.
 */
@Entity(tableName = "fotos_aula", indices = [Index("aulaId")])
data class FotoAula(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val aulaId: Long,
    val nomeArquivo: String,
    val criadaEmMillis: Long = System.currentTimeMillis()
)

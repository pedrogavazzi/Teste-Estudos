package com.pedrogavazzi.controleestudos.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun tipoAlertaParaString(tipo: TipoAlerta): String = tipo.name

    @TypeConverter
    fun stringParaTipoAlerta(valor: String): TipoAlerta =
        runCatching { TipoAlerta.valueOf(valor) }.getOrDefault(TipoAlerta.SOM_E_VIBRACAO)
}
